package com.yf.registry.zk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/2 18:14
 * @version: 1.0.0
 * @url:
 */

@Slf4j
public final class CuratorUtils {
    public static final String ZK_REGISTER_ROOT_PATH = "/myRpc";
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "192.168.64.129";
    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;
    private static CuratorFramework zkClient;
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<String, List<String>>();
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();


    /*
    * @Description
    *   获取 zookeeper 连接
    * @Date 2022/9/2 18:27
    * @param  * @param
    * @return org.apache.curator.framework.CuratorFramework
    **/
    public static CuratorFramework getZkClient(){
        String zookeeperAddress = DEFAULT_ZOOKEEPER_ADDRESS;

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME,MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                .connectString(zookeeperAddress)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        try {
            if (!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)){
                throw new RuntimeException("Time out waiting to connect to ZK!");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return zkClient;
    }

    /*
    * @Description
    *   创建永久结点，用于服务注册
    * @Date 2022/9/2 18:32
    * @param  * @param zkClient
     * @param path
    * @return void
    **/
    public static void createPersistentNode(CuratorFramework zkClient, String path){
        try {
            if (REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null){
                log.info("The node already exists. The node is:[{}]", path);
            } else {
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("The node was created successfully. The node is:[{}]", path);
            }
            REGISTERED_PATH_SET.add(path);

        } catch (Exception e){
            log.error("create persistent node for path [{}] fail", path);
        }

    }

    /*
    * @Description
    *   清除某个服务的某个注册，例如当某台 server 挂了，我们需要在注册中心清除其提供的服务
    * @Date 2022/9/2 19:02
    * @param  * @param zkClient
     * @param inetSocketAddress
    * @return void
    **/
    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress inetSocketAddress){
        REGISTERED_PATH_SET.stream().parallel().forEach(p -> {
            try {
                if (p.endsWith(inetSocketAddress.toString())){
                    zkClient.delete().forPath(p); // 因为我们有监视器 watcher，因此不需要手动更新 SERVICE_ADDRESS_MAP
                }
            } catch (Exception e){
                log.error("clear registry for path [{}] fail", p);
            }
        });
        log.info("All registered services on the server are cleared:[{}]", REGISTERED_PATH_SET.toString());
    }

    public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName){
        if (SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)){
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }

        List<String> result = null;
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        try {
            result = zkClient.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName,result);
            regiterWatcher(rpcServiceName,zkClient);

        } catch (Exception e){
            log.error("get children nodes for path [{}] fail", servicePath);
        }
        return result;

    }

    /*
    * @Description
    *   监听 rpcServiceName 的实现，一旦有新增注册，立刻更新 SERVICE_ADDRESS_MAP
    * @Date 2022/9/2 18:39
    * @param  * @param rpcServiceName
     * @param zkClient
    * @return void
    **/
    private static void regiterWatcher(String rpcServiceName, CuratorFramework zkClient) throws Exception {
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient,servicePath,true);
        PathChildrenCacheListener pathChildrenCacheListener = ((curatorFramework, pathChildrenCacheEvent) -> {
            List<String> serviceAddress = curatorFramework.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName,serviceAddress);
        });
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        pathChildrenCache.start();
    }




}
