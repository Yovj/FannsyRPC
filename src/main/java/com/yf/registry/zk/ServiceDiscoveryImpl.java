package com.yf.registry.zk;

import com.yf.common.enums.RpcErrorMessageEnum;
import com.yf.common.exception.RpcException;
import com.yf.common.extension.ExtensionLoader;
import com.yf.common.factory.SingletonFactory;
import com.yf.common.utils.StringUtil;
import com.yf.config.RpcServiceConfig;
import com.yf.filter.ClientFilter;
import com.yf.filter.client.ClientFilterChain;
import com.yf.filter.client.PermitInvokeFilterImpl;
import com.yf.loadbalance.LoadBalance;
import com.yf.registry.ServiceDiscovery;
import com.yf.remoting.dto.RpcRequest;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.common.StringUtils;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @description:
 *  服务发现
 * @author: Fannsy
 * @date: 2022/9/2 18:14
 * @version: 1.0.0
 * @url:
 */
public class ServiceDiscoveryImpl implements ServiceDiscovery {
    private final LoadBalance balancer;
    public ServiceDiscoveryImpl(){
        ExtensionLoader<LoadBalance> extensionLoader = ExtensionLoader.getExtensionLoader(LoadBalance.class);
        balancer = extensionLoader.getExtension("consistentHashLoadBalance");
    }

    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest,RpcServiceConfig rpcServiceConfig) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<ProviderNodeInfo> serviceProviderList = new ArrayList<>(CuratorUtils.getChildrenNodes(zkClient, rpcServiceName));

        if (serviceProviderList == null || serviceProviderList.isEmpty()){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND,rpcServiceName);
        } else {
            // 路由
            ClientFilterChain clientFilterChain = SingletonFactory.getInstance(ClientFilterChain.class);

            ExtensionLoader<ClientFilter> extensionLoader = ExtensionLoader.getExtensionLoader(ClientFilter.class);
            PermitInvokeFilterImpl permitInvokeFilterImpl = (PermitInvokeFilterImpl) extensionLoader.getExtension("permitInvokeFilterImpl");

            extensionLoader = ExtensionLoader.getExtensionLoader(ClientFilter.class);
            ClientFilter directInvokeFilterImpl = extensionLoader.getExtension("directInvokeFilterImpl");

            clientFilterChain.addClientFilter(directInvokeFilterImpl);
            clientFilterChain.addClientFilter(permitInvokeFilterImpl);

            clientFilterChain.doFilter(serviceProviderList,rpcServiceConfig);

        }
        String targetServiceUrl = balancer.selectServiceAddress(serviceProviderList,rpcRequest);
        if (StringUtil.isEmpty(targetServiceUrl)){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND,rpcServiceName);
        }
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host,port);
    }
}
