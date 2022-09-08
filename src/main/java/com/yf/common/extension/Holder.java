package com.yf.common.extension;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/4 15:40
 * @version: 1.0.0
 * @url:
 */
public class Holder<T> {

    private volatile T value;

    public T get(){
        return value;
    }

    public void set(T value){
        this.value = value;
    }
}
