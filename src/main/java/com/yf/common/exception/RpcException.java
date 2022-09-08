package com.yf.common.exception;

import com.yf.common.enums.RpcErrorMessageEnum;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/2 19:09
 * @version: 1.0.0
 * @url:
 */
public class RpcException extends RuntimeException{
    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum, String detail){
        super(rpcErrorMessageEnum.getMessage() + ":" + detail);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum) {
        super(rpcErrorMessageEnum.getMessage());
    }

}
