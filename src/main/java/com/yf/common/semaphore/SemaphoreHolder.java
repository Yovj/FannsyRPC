package com.yf.common.semaphore;

import lombok.Data;

import java.util.concurrent.Semaphore;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/7 15:14
 * @version: 1.0.0
 * @url:
 */
@Data
public class SemaphoreHolder {
    private Semaphore semaphore;

    private int maxNums;

    public SemaphoreHolder(int maxNums){
        this.maxNums = maxNums;
        semaphore = new Semaphore(maxNums);
    }

}
