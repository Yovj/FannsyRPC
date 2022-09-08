package com.yf.common.utils;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/4 16:16
 * @version: 1.0.0
 * @url:
 */
public class StringUtil {
    public static boolean isBlank(String s){
        if (s == null || s.length() == 0){
            return true;
        }

        for (int i = 0; i < s.length(); i++) {
            if (!Character.isWhitespace(s.charAt(i))){
                return false;
            }
        }
        return true;

    }

    public static boolean isEmpty(String s){
        if (s == null || s.length() == 0){
            return true;
        }
        return false;

    }

}
