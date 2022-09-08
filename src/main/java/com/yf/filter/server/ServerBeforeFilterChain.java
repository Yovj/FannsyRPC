package com.yf.filter.server;

import com.yf.filter.ServerFilter;
import com.yf.remoting.dto.RpcRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/7 10:07
 * @version: 1.0.0
 * @url:
 */
public class ServerBeforeFilterChain extends ServerFilterChain{
    private static List<ServerFilter> ServerFilters = new ArrayList<>();

    @Override
    public void addServerFilter(ServerFilter serverFilter) {
        ServerFilters.add(serverFilter);
    }

    @Override
    public void doFilter(RpcRequest rpcRequest) {
        ServerFilters.forEach(serverFilter ->{
            serverFilter.doFilter(rpcRequest);
        });
    }
}
