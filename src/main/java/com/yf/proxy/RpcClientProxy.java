package com.yf.proxy;

import com.yf.common.enums.RpcErrorMessageEnum;
import com.yf.common.enums.RpcResponseCodeEnum;
import com.yf.common.exception.RpcException;
import com.yf.config.RpcServiceConfig;
import com.yf.remoting.dto.RpcRequest;
import com.yf.remoting.dto.RpcResponse;
import com.yf.transport.RpcRequestTransport;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/3 20:54
 * @version: 1.0.0
 * @url:
 */

@Slf4j
public class RpcClientProxy implements InvocationHandler {
    private static final String INTERFACE_NAME = "interfaceName";
    private final RpcServiceConfig rpcServiceConfig;
    private final RpcRequestTransport rpcRequestTransport;

    public RpcClientProxy(RpcServiceConfig rpcServiceConfig, RpcRequestTransport rpcRequestTransport){
        this.rpcRequestTransport = rpcRequestTransport;
        this.rpcServiceConfig = rpcServiceConfig;
    }


    /*
    * 获取代理对象
    **/
    public <T>T getProxy(Class<T> clazz){
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[] {clazz}, this);
    }

    /*
    * 重写 invoke
    * 向 zookeeper 拉取服务列表
    * 给查询到的 server 发信息，获取方法执行结果
    **/
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("invoked method: [{}]", method.getName());
        RpcRequest rpcRequest = RpcRequest.builder().methodName(method.getName())
                .parameters(args)
                .paramTypes(method.getParameterTypes())
                .interfaceName(method.getDeclaringClass().getName())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion())
                .requestId(UUID.randomUUID().toString())
                .token(rpcServiceConfig.getToken())
                .build();
        RpcResponse<Object> rpcResponse = null;

        CompletableFuture<Object> completableFuture = (CompletableFuture<Object>) rpcRequestTransport.sendRpcRequest(rpcRequest);
        rpcResponse = (RpcResponse<Object>) completableFuture.get();

        this.check(rpcResponse,rpcRequest);
        return rpcResponse.getData();
    }

    private void check(RpcResponse<Object> rpcResponse, RpcRequest rpcRequest){
        if (rpcResponse == null){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE);
        }
        if (! rpcRequest.getRequestId().equals(rpcResponse.getRequestId())){
            throw new RpcException(RpcErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE);
        }
        if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCodeEnum.SUCCESS.getCode())){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
        }
    }

}
