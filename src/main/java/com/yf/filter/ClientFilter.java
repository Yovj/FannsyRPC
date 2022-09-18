package com.yf.filter;

import com.yf.common.extension.SPI;
import com.yf.config.RpcServiceConfig;
import com.yf.registry.zk.ProviderNodeInfo;
import com.yf.remoting.dto.RpcMessage;
import com.yf.remoting.dto.RpcRequest;
import com.yf.remoting.dto.RpcResponse;

import java.util.List;

@SPI
public interface ClientFilter extends Filter{

    void doFilter(List<ProviderNodeInfo> providerNodeInfoList, RpcServiceConfig rpcServiceConfig);
}
