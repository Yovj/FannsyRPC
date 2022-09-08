package com.yf.remoting.dto;

import lombok.*;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/2 15:11
 * @version: 1.0.0
 * @url:
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Getter
public class RpcRequest {
    private String requestId;
    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    private String version;
    private Class<?>[] paramTypes;
    private String group;
    private String token;

    public String getRpcServiceName() {
        return this.getInterfaceName() + this.getGroup() + this.version;
    }
}
