package com.yf.filter.client;

import com.yf.config.RpcServiceConfig;
import com.yf.filter.ClientFilter;
import com.yf.registry.zk.CuratorUtils;
import com.yf.registry.zk.ProviderNodeInfo;
import com.yf.remoting.dto.RpcRequest;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/8 15:38
 * @version: 1.0.0
 * @url:
 */
public class PermitInvokeFilterImpl implements ClientFilter {



    @Override
    public void doFilter(List<ProviderNodeInfo> providerNodeInfoList, RpcServiceConfig rpcServiceConfig) {
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        Iterator<ProviderNodeInfo> iterator = providerNodeInfoList.iterator();
        while (iterator.hasNext()){
            ProviderNodeInfo providerNodeInfo = iterator.next();
            Set<String> servicePermitSet = new HashSet<>(CuratorUtils.getPermitList(zkClient, providerNodeInfo.getRpcServiceName(), providerNodeInfo.getServiceAddr()));
            try {
                String hostAddress = InetAddress.getLocalHost().getHostAddress();
                if (!(servicePermitSet == null || servicePermitSet.isEmpty() || servicePermitSet.contains(hostAddress))){
                    iterator.remove();
                }
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
