package com.yf.remoting.handler;

import com.yf.common.factory.SingletonFactory;
import com.yf.provider.ServiceProvider;
import com.yf.provider.impl.ZkServiceProvider;
import com.yf.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/3 11:07
 * @version: 1.0.0
 * @url:
 */
@Slf4j
public class RpcRequestHandler {
    private final ServiceProvider serviceProvider;

    private RpcRequestHandler(){
        serviceProvider = SingletonFactory.getInstance(ZkServiceProvider.class);
    }

    public Object handle(RpcRequest rpcRequest){
        Object service = serviceProvider.getService(rpcRequest.getRpcServiceName());
        return invokeTargetMethod(rpcRequest, service);
    }

    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result;

        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(),rpcRequest.getParamTypes());
            result = method.invoke(service,rpcRequest.getParameters());
            log.info("service:[{}] successful invoke method:[{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

}
