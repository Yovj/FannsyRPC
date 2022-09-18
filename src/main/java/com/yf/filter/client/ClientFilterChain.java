package com.yf.filter.client;

import com.yf.config.RpcServiceConfig;
import com.yf.filter.ClientFilter;
import com.yf.registry.zk.ProviderNodeInfo;
import com.yf.remoting.dto.RpcRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/8 15:26
 * @version: 1.0.0
 * @url:
 */
public class ClientFilterChain {

    private static List<ClientFilter> clientFilterList = new ArrayList<>();

    public void addClientFilter(ClientFilter clientFilter) {
        clientFilterList.add(clientFilter);
    }

    public void doFilter(List<ProviderNodeInfo> providerNodeInfoList, RpcServiceConfig rpcServiceConfig) {
        clientFilterList.forEach(clientFilter -> {
            clientFilter.doFilter(providerNodeInfoList,rpcServiceConfig);
        });
    }


}
