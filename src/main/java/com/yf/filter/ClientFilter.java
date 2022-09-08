package com.yf.filter;

import com.yf.common.extension.SPI;
import com.yf.remoting.dto.RpcMessage;
import com.yf.remoting.dto.RpcResponse;

@SPI
public interface ClientFilter extends Filter{

    void doFilter(RpcResponse rpcResponse);
}
