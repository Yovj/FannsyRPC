package com.yf.loadbalance;

import com.yf.common.extension.SPI;
import com.yf.registry.zk.ProviderNodeInfo;
import com.yf.remoting.dto.RpcRequest;

import java.util.List;
@SPI
public interface LoadBalance {
    String selectServiceAddress(List<ProviderNodeInfo> serviceUrlList, RpcRequest rpcRequest);
}
