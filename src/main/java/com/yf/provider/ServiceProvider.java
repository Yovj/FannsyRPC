package com.yf.provider;

import com.yf.config.RpcServiceConfig;

public interface ServiceProvider {

    void addService(RpcServiceConfig rpcServiceConfig);

    Object getService(String rpcServiceName);

    void publishService(RpcServiceConfig rpcServiceConfig);

}
