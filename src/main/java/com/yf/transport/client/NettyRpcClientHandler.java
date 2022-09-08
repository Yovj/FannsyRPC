package com.yf.transport.client;

import com.yf.common.enums.CompressTypeEnum;
import com.yf.common.enums.SerializationTypeEnum;
import com.yf.common.factory.SingletonFactory;
import com.yf.remoting.constants.RpcConstants;
import com.yf.remoting.dto.RpcMessage;
import com.yf.remoting.dto.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/4 6:51
 * @version: 1.0.0
 * @url:
 */
@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {
    private final UnprocessedRequests unprocessedRequests;
    private final NettyRpcClient nettyRpcClient;
    public NettyRpcClientHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            log.info("client receive msg: [{}]", msg);
            if (msg instanceof RpcMessage) {
                RpcMessage tmp = (RpcMessage) msg;
                byte messageType = tmp.getMessageType();
                if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                    log.info("heart [{}]", tmp.getData());
                } else if (messageType == RpcConstants.RESPONSE_TYPE) {
                    RpcResponse<Object> rpcResponse = (RpcResponse<Object>) tmp.getData();
                    unprocessedRequests.complete(rpcResponse);
                }
            }

        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());

                RpcMessage rpcMessage = RpcMessage.builder()
                        .messageType(RpcConstants.HEARTBEAT_REQUEST_TYPE)
                        .codec(SerializationTypeEnum.KYRO.getCode())
                        .compress(CompressTypeEnum.GZIP.getCode())
                        .data(RpcConstants.PING)
                        .build();
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);

//                channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
//                    System.out.println("send success!");
//                });

            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        nettyRpcClient.doConnect((InetSocketAddress) ctx.channel().remoteAddress());
        super.channelInactive(ctx);
    }
}
