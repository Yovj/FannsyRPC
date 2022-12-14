package com.yf.registry;

import com.yf.config.RpcServiceConfig;
import com.yf.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;

public interface ServiceDiscovery {
    public InetSocketAddress lookupService(RpcRequest rpcRequest, RpcServiceConfig rpcServiceConfig);
}
