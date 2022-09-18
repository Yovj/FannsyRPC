package com.yf.filter.server;

import com.yf.common.factory.SpringBeanFactory;
import com.yf.common.semaphore.SemaphoreHolder;
import com.yf.common.enums.RpcErrorMessageEnum;
import com.yf.common.exception.RpcException;
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
 * @date: 2022/9/7 15:23
 * @version: 1.0.0
 * @url:
 */
@Slf4j
public class ServerServiceBeforeLimitFilterImpl implements ServerFilter {

    private NettyRpcServer nettyRpcServer;

    @Override
    public void doFilter(RpcRequest rpcRequest) {
        if (nettyRpcServer == null){
            nettyRpcServer = SpringBeanFactory.getBean(NettyRpcServer.class);
        }
        String serviceName = rpcRequest.getRpcServiceName();

        Map<String, SemaphoreHolder> semaphoreHolderMap = nettyRpcServer.getSemaphoreHolderMap();
        SemaphoreHolder semaphoreHolder = semaphoreHolderMap.get(serviceName);
        Semaphore semaphore = semaphoreHolder.getSemaphore();

//        System.out.println("get semaphore:" + semaphore);
        if (!semaphore.tryAcquire()){
            log.error("[ServerServiceBeforeLimitFilterImpl] {}'s max request is {},reject now", rpcRequest.getRpcServiceName(), semaphoreHolder.getMaxNums());
            throw new RpcException(RpcErrorMessageEnum.MaxServiceLimitRequestException);
        }
    }
}
