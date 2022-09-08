package com.yf.registry.zk;

import com.yf.common.enums.RpcErrorMessageEnum;
import com.yf.common.exception.RpcException;
import com.yf.common.extension.ExtensionLoader;
import com.yf.loadbalance.LoadBalance;
import com.yf.registry.ServiceDiscovery;
import com.yf.remoting.dto.RpcRequest;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;
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
        balancer = extensionLoader.getExtension("loadBalance");
    }

    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);

        if (serviceUrlList.isEmpty()){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND,rpcServiceName);
        }
        String targetServiceUrl = balancer.selectServiceAddress(serviceUrlList,rpcRequest);
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host,port);
    }
}
