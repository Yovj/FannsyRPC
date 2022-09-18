package com.yf.common.utils;

import com.google.common.net.InetAddresses;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/8 17:01
 * @version: 1.0.0
 * @url:
 */
public class IpUtil {

    public static boolean isValidIp(String ip) {
        return InetAddresses.isInetAddress(ip);
    }
}
