package com.yf.registry;

import java.net.InetSocketAddress;

public interface ServiceRegistry {
    void registerService(InetSocketAddress inetSocketAddress, String rpcServiceName);
}
