package com.yf.filter.server;

import com.yf.common.factory.SpringBeanFactory;
import com.yf.common.semaphore.SemaphoreHolder;
import com.yf.common.factory.SingletonFactory;
import com.yf.filter.ServerFilter;
import com.yf.remoting.dto.RpcRequest;
import com.yf.transport.server.NettyRpcServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/7 15:36
 * @version: 1.0.0
 * @url:
 */
@Slf4j
public class ServerServiceAfterLimitFilterImpl implements ServerFilter {


    private NettyRpcServer nettyRpcServer;

    @Override
    public void doFilter(RpcRequest rpcRequest) {
        if (null == rpcRequest){
            return;
        }
        if (nettyRpcServer == null){
            nettyRpcServer = SpringBeanFactory.getBean(NettyRpcServer.class);
        }

        try {
            String serviceName = rpcRequest.getRpcServiceName();
            Map<String, SemaphoreHolder> semaphoreHolderMap = nettyRpcServer.getSemaphoreHolderMap();
            SemaphoreHolder semaphoreHolder = semaphoreHolderMap.get(serviceName);
            Semaphore semaphore = semaphoreHolder.getSemaphore();
            semaphore.release();
//            System.out.println("release semaphore:" + semaphore);
        } catch (Exception e){
            log.error(e.getMessage());
            throw new IllegalStateException();
        }
    }
}
