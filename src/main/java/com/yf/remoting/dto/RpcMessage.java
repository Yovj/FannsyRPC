package com.yf.remoting.dto;

import lombok.*;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/3 8:17
 * @version: 1.0.0
 * @url:
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcMessage {

    private byte messageType;

    private byte codec;

    private byte compress;

    private int requestId;

    private Object data;

}
