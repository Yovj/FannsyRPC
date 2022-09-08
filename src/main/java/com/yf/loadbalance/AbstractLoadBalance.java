package com.yf.loadbalance;

import com.yf.remoting.dto.RpcRequest;

import java.util.List;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/5 8:16
 * @version: 1.0.0
 * @url:
 */
public abstract class AbstractLoadBalance implements LoadBalance{

    @Override
    public String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest) {
        // 进行一个简单的初筛
        if (serviceUrlList == null || serviceUrlList.isEmpty()){
            return null;
        }
        if (serviceUrlList.size() == 1){
            return serviceUrlList.get(0);
        }
        return doSelect(serviceUrlList,rpcRequest);
    }

    protected abstract String doSelect(List<String> serviceUrlList, RpcRequest rpcRequest);

}
