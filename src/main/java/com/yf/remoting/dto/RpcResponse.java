package com.yf.remoting.dto;

import com.yf.common.enums.RpcResponseCodeEnum;
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
@Getter
@Setter
@Builder
@ToString
public class RpcResponse<T> {

    private String requestId;
    private Integer code;
    private String message;
    private T data;

    public static <T> RpcResponse<T> success(T data, String requestId){
        RpcResponse<T> response = (RpcResponse<T>) RpcResponse.builder()
                .code(RpcResponseCodeEnum.SUCCESS.getCode())
                .message(RpcResponseCodeEnum.SUCCESS.getMsg())
                .requestId(requestId)
                .data(data)
                .build();
        return response;
    }

    public static <T> RpcResponse<T> fail(RpcResponseCodeEnum rpcResponseCodeEnum){
        RpcResponse<T> response = (RpcResponse<T>) RpcResponse.builder()
                .code(RpcResponseCodeEnum.FAIL.getCode())
                .message(RpcResponseCodeEnum.FAIL.getMsg())
                .build();
        return response;
    }




}
