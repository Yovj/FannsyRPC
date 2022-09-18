package com.yf.registry.zk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/8 8:42
 * @version: 1.0.0
 * @url:
 */
@Data
@AllArgsConstructor
@Builder
@ToString
public class ProviderNodeInfo {
    private String rpcServiceName;
    private String serviceAddr;
    private Integer weight;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProviderNodeInfo){
            ProviderNodeInfo objProvider = (ProviderNodeInfo) obj;
            return this.rpcServiceName.equals(objProvider.getRpcServiceName()) && this.getServiceAddr().equals(objProvider.getServiceAddr());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return rpcServiceName.hashCode() + serviceAddr.hashCode();
    }
}
