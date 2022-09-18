package com.yf.filter.client;

import com.yf.common.utils.IpUtil;
import com.yf.common.utils.StringUtil;
import com.yf.config.RpcServiceConfig;
import com.yf.filter.ClientFilter;
import com.yf.registry.zk.ProviderNodeInfo;
import com.yf.remoting.dto.RpcRequest;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/8 17:18
 * @version: 1.0.0
 * @url:
 */
public class DirectInvokeFilterImpl implements ClientFilter {
    @Override
    public void doFilter(List<ProviderNodeInfo> providerNodeInfoList, RpcServiceConfig rpcServiceConfig) {
        List<String> directIps = rpcServiceConfig.getDirectIp();
        if (directIps == null || directIps.isEmpty()){
            return;
        }

        Set<String>  directIpSet = new HashSet<>(directIps);
        Iterator<ProviderNodeInfo> iterator = providerNodeInfoList.iterator();
        while (iterator.hasNext()){
            ProviderNodeInfo providerNodeInfo = iterator.next();
            String serviceAddr = providerNodeInfo.getServiceAddr();
            String serviceIp = serviceAddr.split(":")[0];
            if (!IpUtil.isValidIp(serviceIp)){
                throw new IllegalArgumentException("必须为 Ip 格式");
            }
            if (!directIpSet.contains(serviceIp)){
                iterator.remove();
            }
        }
    }
}
