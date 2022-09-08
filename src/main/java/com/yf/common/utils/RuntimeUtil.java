package com.yf.common.utils;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/7 16:07
 * @version: 1.0.0
 * @url:
 */
public class RuntimeUtil {
    public static int cpus(){
        return Runtime.getRuntime().availableProcessors();
    }

}
