package com.yf.registry;

import com.yf.config.RpcServiceConfig;

import java.net.InetSocketAddress;

public interface ServiceRegistry {
    void registerService(InetSocketAddress inetSocketAddress, RpcServiceConfig rpcServiceConfig);
}
