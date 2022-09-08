package com.yf.provider.impl;

import com.yf.common.enums.RpcErrorMessageEnum;
import com.yf.common.exception.RpcException;
import com.yf.config.RpcServiceConfig;
import com.yf.provider.ServiceProvider;
import com.yf.registry.ServiceRegistry;
import com.yf.registry.zk.ServiceRegisterImpl;
import com.yf.transport.server.NettyRpcServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/3 10:34
 * @version: 1.0.0
 * @url:
 */
@Slf4j
public class ZkServiceProvider implements ServiceProvider {
    private final Map<String,Object> serviceMap;
    private final Set<String> registeredService;
    private final ServiceRegistry serviceRegistry;

    private ZkServiceProvider(){
        serviceMap = new ConcurrentHashMap<>();
        registeredService = ConcurrentHashMap.newKeySet();
        serviceRegistry = new ServiceRegisterImpl(); // TODO 改为 extension 加载
    }

    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        if (registeredService.contains(rpcServiceName)) return;
        registeredService.add(rpcServiceName);
        System.out.println(Thread.currentThread().getName());
        serviceMap.put(rpcServiceName,rpcServiceConfig.getService());
        log.info("Add service: {} and interfaces:{}", rpcServiceName, rpcServiceConfig.getService().getClass().getInterfaces());

    }

    @Override
    public Object getService(String rpcServiceName) {
        System.out.println(Thread.currentThread().getName());
        Object service = serviceMap.get(rpcServiceName);
        if (null == service){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            this.addService(rpcServiceConfig);
            serviceRegistry.registerService(new InetSocketAddress(host, NettyRpcServer.PORT),rpcServiceConfig.getRpcServiceName());

        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }

    }
}
