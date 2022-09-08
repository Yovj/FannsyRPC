package com.yf.transport;

import com.yf.remoting.dto.RpcRequest;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/3 21:07
 * @version: 1.0.0
 * @url:
 */
public interface RpcRequestTransport {
    Object sendRpcRequest(RpcRequest rpcRequest);
}
