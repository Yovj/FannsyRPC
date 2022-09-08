package com.yf.loadbalance.balancer;

import com.yf.loadbalance.AbstractLoadBalance;
import com.yf.remoting.dto.RpcRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/5 8:20
 * @version: 1.0.0
 * @url:
 */
public class ConsistentHashLoadBalance extends AbstractLoadBalance {

    private final Map<String, ConsistentHashSelector> selectors = new ConcurrentHashMap<>();



    static class ConsistentHashSelector{
        private final int identityHashCode;

        private final TreeMap<Long, String> virtualInvokers;

        ConsistentHashSelector(List<String> invokers, int replicaNumber, int identityHashCode){
            this.identityHashCode = identityHashCode;
            virtualInvokers = new TreeMap<>();

            invokers.forEach((invoker) -> {
                for (int i = 0; i < replicaNumber / 4; i++) {
                    byte[] digest = md5(invoker + i);
                    for (int h = 0; h < 4; h++) {
                        long m = hash(digest, h);
                        virtualInvokers.put(m, invoker);
                    }
                }
            });

        }

        static long hash(byte[] digest, int idx) {
            return ((long) (digest[3 + idx * 4] & 255) << 24 | (long) (digest[2 + idx * 4] & 255) << 16 | (long) (digest[1 + idx * 4] & 255) << 8 | (long) (digest[idx * 4] & 255)) & 4294967295L;
        }

        /*
        * 一种被广泛使用的密码散列函数，可以产生出一个128位（16字节）的散列值（hash value）
        **/
        static byte[] md5(String key) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                md.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            return md.digest();
        }

        public String select(String rpcServiceKey){
            byte[] digest = md5(rpcServiceKey);
            return selectForKey(hash(digest,0));
        }

        private String selectForKey(long hash) {
            Map.Entry<Long, String> entry = virtualInvokers.tailMap(hash, true).firstEntry();
            if (entry == null){
                entry = virtualInvokers.firstEntry();
            }
            return entry.getValue();
        }

    }
    @Override
    protected String doSelect(List<String> serviceUrlList, RpcRequest rpcRequest) {
        int identityHashCode = System.identityHashCode(serviceUrlList);
        String rpcServiceName = rpcRequest.getRpcServiceName();

        ConsistentHashSelector selector = selectors.get(rpcServiceName);
        if (selector == null || selector.identityHashCode != identityHashCode){
            selectors.putIfAbsent(rpcServiceName,new ConsistentHashSelector(serviceUrlList,160,identityHashCode));
            selector = selectors.get(rpcServiceName);
        }
        return selector.select(rpcServiceName + Arrays.stream(rpcRequest.getParameters()));
    }
}
