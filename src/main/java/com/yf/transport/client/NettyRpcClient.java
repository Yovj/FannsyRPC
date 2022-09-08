package com.yf.transport.client;

import com.yf.common.enums.CompressTypeEnum;
import com.yf.common.enums.RpcErrorMessageEnum;
import com.yf.common.enums.SerializationTypeEnum;
import com.yf.common.exception.RpcException;
import com.yf.common.factory.SingletonFactory;
import com.yf.registry.ServiceDiscovery;
import com.yf.registry.zk.ServiceDiscoveryImpl;
import com.yf.remoting.constants.RpcConstants;
import com.yf.remoting.dto.RpcMessage;
import com.yf.remoting.dto.RpcRequest;
import com.yf.remoting.dto.RpcResponse;
import com.yf.transport.RpcRequestTransport;
import com.yf.transport.codec.RpcMessageDecoder;
import com.yf.transport.codec.RpcMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/2 16:28
 * @version: 1.0.0
 * @url:
 */@Slf4j
public class NettyRpcClient implements RpcRequestTransport {
    private final ServiceDiscovery serviceDiscovery;
    private final UnprocessedRequests unprocessedRequests;
    private final ChannelProvider channelProvider;
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    private final AtomicInteger reconnectionTime = new AtomicInteger(RpcConstants.MAX_RECONNECTION_TIMES);


    private NettyRpcClient(){
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();

        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        ChannelPipeline pipeline = nioSocketChannel.pipeline();
                        pipeline.addLast(new IdleStateHandler(0,5,0, TimeUnit.SECONDS));
                        pipeline.addLast(new RpcMessageEncoder());
                        pipeline.addLast(new RpcMessageDecoder());
                        pipeline.addLast(new NettyRpcClientHandler());
                    }
                });
        this.serviceDiscovery = new ServiceDiscoveryImpl();
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
    }

    public Channel doConnect(InetSocketAddress inetSocketAddress) throws ExecutionException, InterruptedException {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();

        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()){
                reconnectionTime.set(RpcConstants.MAX_RECONNECTION_TIMES); // 连接成功，重置最大连接次数
                log.info("The client has connected [{}] successful!", inetSocketAddress.toString());
                completableFuture.complete(future.channel());
            }else {
                final EventLoop loop = future.channel().eventLoop();
                if (reconnectionTime.getAndDecrement() > 0){
                    loop.schedule(new Runnable() {
                        @Override
                        public void run() {
                            System.err.println("服务端链接不上，第 " + (RpcConstants.MAX_RECONNECTION_TIMES - reconnectionTime.get()) + " 次重新连接...");
                            try {
                                doConnect(inetSocketAddress);
                            } catch (ExecutionException e) {
                                throw new RuntimeException(e);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }, 2L, TimeUnit.SECONDS);
                } else {
                    throw new RpcException(RpcErrorMessageEnum.CLIENT_CONNECT_SERVER_FAILURE);
                }
            }
        });
        return completableFuture.get();
    }

    public Channel getChannel(InetSocketAddress inetSocketAddress) throws ExecutionException, InterruptedException {
        Channel channel = channelProvider.getChannel(inetSocketAddress);
        if (channel == null){
            channel = doConnect(inetSocketAddress);
            channelProvider.setChannel(inetSocketAddress,channel);
        }
        return channel;
    }

    public Object sendRpcRequest(RpcRequest rpcRequest) {
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);

        Channel channel = null;
        try {
            channel = getChannel(inetSocketAddress);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (channel.isActive()){
            unprocessedRequests.put(rpcRequest.getRequestId(),resultFuture); // client 收到 server 发来的 response 后，在回调函数中取出 resultFuture

            RpcMessage rpcMessage = RpcMessage.builder().data(rpcRequest)
                    .codec(SerializationTypeEnum.KYRO.getCode())
                    .compress(CompressTypeEnum.GZIP.getCode())
                    .messageType(RpcConstants.REQUEST_TYPE)
                    .build();

            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()){
                log.info("client send message: [{}]", rpcMessage);
            }else {
                future.channel().close();
                resultFuture.completeExceptionally(future.cause());
                log.error("Send failed:", future.cause());
            }});

        }else {
            throw new IllegalStateException();
        }
        return resultFuture;
    }


}
