package com.yf.transport.server;

import com.yf.common.semaphore.SemaphoreHolder;
import com.yf.common.factory.SingletonFactory;
import com.yf.common.utils.RuntimeUtil;
import com.yf.common.utils.StringUtil;
import com.yf.common.utils.concurrent.threadpool.ThreadPoolFactoryUtil;
import com.yf.config.RpcServiceConfig;
import com.yf.provider.ServiceProvider;
import com.yf.provider.impl.ZkServiceProvider;
import com.yf.remoting.constants.RpcConstants;
import com.yf.transport.codec.RpcMessageDecoder;
import com.yf.transport.codec.RpcMessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/3 10:31
 * @version: 1.0.0
 * @url:
 */
@Slf4j
public class NettyRpcServer {
    public static final int PORT = 9998;

    private final ServiceProvider serviceProvider = SingletonFactory.getInstance(ZkServiceProvider.class);

    private final Map<String, SemaphoreHolder> semaphoreHolderMap = new ConcurrentHashMap<>();

    private static Map<String, String> TOKEN_MAP = new ConcurrentHashMap<>();

    private NettyRpcServer(){

    }

    public Map<String, SemaphoreHolder> getSemaphoreHolderMap(){
        return this.semaphoreHolderMap;
    }

    public void registerService(RpcServiceConfig rpcServiceConfig){
        serviceProvider.publishService(rpcServiceConfig);
        String serviceToken = rpcServiceConfig.getToken();
        if (StringUtil.isEmpty(serviceToken)){
            return;
        }
        TOKEN_MAP.put(rpcServiceConfig.getRpcServiceName(),serviceToken);
        semaphoreHolderMap.put(rpcServiceConfig.getRpcServiceName(),new SemaphoreHolder(RpcConstants.MAX_SEMAPHORE_NUMS));
    }

    public String getServiceToken(String serviceName){
        if (TOKEN_MAP.containsKey(serviceName)){
            return TOKEN_MAP.get(serviceName);
        }
        return null;
    }

    public void start() throws UnknownHostException {
        // TODO ： 清空原有服务

        String host = InetAddress.getLocalHost().getHostAddress();
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup(5);
        ServerBootstrap bootstrap = new ServerBootstrap();

        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(RuntimeUtil.cpus() * 2,
                ThreadPoolFactoryUtil.createThreadFactory("service-handler-group",false));

        try {
            bootstrap.group(boss,worker)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    .option(ChannelOption.SO_BACKLOG, RpcConstants.BACKLOG) // 服务端限流
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            // 30 秒之内没有收到客户端请求的话就关闭连接
                            pipeline.addLast(new IdleStateHandler(30,0,0, TimeUnit.SECONDS));
                            pipeline.addLast(new RpcMessageEncoder());
                            pipeline.addLast(new RpcMessageDecoder());
                            pipeline.addLast(serviceHandlerGroup,new NettyRpcServerHandler());

                        }
                    });
            ChannelFuture future = bootstrap.bind(host,PORT).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e){

        } finally {
            log.error("shutdown bossGroup and workerGroup");
            boss.shutdownGracefully();
            worker.shutdownGracefully();
//            serviceHandlerGroup.shutdownGracefully();
        }



    }

}
