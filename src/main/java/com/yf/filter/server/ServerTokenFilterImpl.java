package com.yf.filter.server;

import com.yf.common.enums.RpcErrorMessageEnum;
import com.yf.common.exception.RpcException;
import com.yf.common.factory.SingletonFactory;
import com.yf.common.factory.SpringBeanFactory;
import com.yf.common.utils.StringUtil;
import com.yf.filter.ServerFilter;
import com.yf.remoting.dto.RpcRequest;
import com.yf.transport.server.NettyRpcServer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/7 10:10
 * @version: 1.0.0
 * @url:
 */

public class ServerTokenFilterImpl implements ServerFilter {

    private NettyRpcServer nettyRpcServer;

    @Override
    public void doFilter(RpcRequest rpcRequest) {
        if (nettyRpcServer == null){
            nettyRpcServer = SpringBeanFactory.getBean(NettyRpcServer.class);
        }


        String token = rpcRequest.getToken();
        String matchToken = nettyRpcServer.getServiceToken(rpcRequest.getRpcServiceName());
//        System.out.println("token:" + token);
        if (StringUtil.isEmpty(matchToken)){
            return;
        }

        if (!StringUtil.isEmpty(token) && token.equals(matchToken)){
            return;
        }else {
            throw new RpcException(RpcErrorMessageEnum.TOKEN_NOT_MATCH);
        }
    }
}
