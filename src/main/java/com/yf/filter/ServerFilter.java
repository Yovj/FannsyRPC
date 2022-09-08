package com.yf.filter;

import com.yf.common.extension.SPI;
import com.yf.remoting.dto.RpcRequest;

@SPI
public interface ServerFilter extends Filter{

    void doFilter(RpcRequest rpcRequest);
}
