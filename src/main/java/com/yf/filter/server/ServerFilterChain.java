package com.yf.filter.server;

import com.yf.filter.ServerFilter;
import com.yf.remoting.dto.RpcRequest;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/7 9:33
 * @version: 1.0.0
 * @url:
 */
public abstract class ServerFilterChain {

    public abstract void addServerFilter(ServerFilter serverFilter);

    public abstract void doFilter(RpcRequest rpcRequest);
}
