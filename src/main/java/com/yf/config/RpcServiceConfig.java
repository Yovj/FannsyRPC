package com.yf.config;

import com.yf.common.utils.IpUtil;
import lombok.*;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/3 10:35
 * @version: 1.0.0
 * @url:
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcServiceConfig {
    /**
     * service version
     */
    private String version = "";
    /**
     * when the interface has multiple implementation classes, distinguish by group
     */
    private String group = "";

    /**
     * target service
     */
    private Object service;

    private String token;

    private Set<String> permitIps = new HashSet<>();

    private List<String> directIp;

    public void addPermit(String inetAddress){
        if (IpUtil.isValidIp(inetAddress)){
            permitIps.add(inetAddress);
        } else {
            throw new IllegalArgumentException("invalid ip address .");
        }

    }

    public void removePermit(InetSocketAddress inetSocketAddress){
        if (inetSocketAddress != null){
            permitIps.remove(inetSocketAddress);
        }
    }

    public void clearPermit(){
        permitIps.clear();
    }

    public String getRpcServiceName(){
        return this.getServiceName() + this.getGroup() + this.getVersion();
    }

    public String getServiceName(){
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }



}
