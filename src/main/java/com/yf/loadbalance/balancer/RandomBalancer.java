package com.yf.loadbalance.balancer;

import com.yf.loadbalance.AbstractLoadBalance;
import com.yf.remoting.dto.RpcRequest;

import java.util.List;
import java.util.Random;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/5 8:19
 * @version: 1.0.0
 * @url:
 */
public class RandomBalancer extends AbstractLoadBalance {
    @Override
    protected String doSelect(List<String> serviceUrlList, RpcRequest rpcRequest) {
        Random random = new Random();
        return serviceUrlList.get(random.nextInt(serviceUrlList.size()));
    }
}
