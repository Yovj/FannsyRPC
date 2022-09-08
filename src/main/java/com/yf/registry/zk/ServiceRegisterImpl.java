package com.yf.registry.zk;

import com.yf.registry.ServiceRegistry;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;

/**
 * @description:
 *  zookeeper 服务注册
 * @author: Fannsy
 * @date: 2022/9/2 18:13
 * @version: 1.0.0
 * @url:
 */
public class ServiceRegisterImpl implements ServiceRegistry {
    @Override
    public void registerService(InetSocketAddress inetSocketAddress, String rpcServiceName) {
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.createPersistentNode(zkClient,servicePath);
    }
}
