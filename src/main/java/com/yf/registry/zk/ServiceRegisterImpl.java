package com.yf.registry.zk;

import com.yf.common.utils.StringUtil;
import com.yf.config.RpcServiceConfig;
import com.yf.registry.ServiceRegistry;
import com.yf.remoting.constants.RpcConstants;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.Set;

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
    public void registerService(InetSocketAddress inetSocketAddress, RpcServiceConfig rpcServiceConfig) {
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.createPersistentNode(zkClient,servicePath, RpcConstants.SERVICE_NODE);

        // 灰度发布，只允许这几个 ip 调用 server 的服务
        Set<String> permitIps = rpcServiceConfig.getPermitIps();
        if (permitIps == null || permitIps.isEmpty()){
            return;
        }
        permitIps.forEach(permitIp -> {
            String permitPath = servicePath + "/permit/" + permitIp;
            CuratorUtils.createPersistentNode(zkClient,permitPath,RpcConstants.PERMIT_NODE);
        });
    }
}
