package com.yf.filter.server;

import com.google.common.util.concurrent.RateLimiter;
import com.yf.common.enums.RpcErrorMessageEnum;
import com.yf.common.exception.RpcException;
import com.yf.common.factory.SingletonFactory;
import com.yf.common.factory.SpringBeanFactory;
import com.yf.common.semaphore.SemaphoreHolder;
import com.yf.filter.ServerFilter;
import com.yf.remoting.constants.RpcConstants;
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
 * @date: 2022/9/9 17:45
 * @version: 1.0.0
 * @url:
 */
@Slf4j
public class ServerServiceBeforeRateLimitFilterImpl implements ServerFilter {

    private static final RateLimiter rateLimiter = RateLimiter.create(RpcConstants.RATELIMIT);

    @Override
    public void doFilter(RpcRequest rpcRequest) {
        if (!rateLimiter.tryAcquire()){
            log.error("[ServerServiceBeforeLimitFilterImpl] {}'s max request is {},reject now", rpcRequest.getRpcServiceName(), rateLimiter.getRate());
            throw new RpcException(RpcErrorMessageEnum.MaxServiceLimitRequestException);
        }
    }
}
