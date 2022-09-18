package com.yf.service.impl;

import com.yf.service.HelloService;
import com.yf.spring.annotaion.RpcService;
import lombok.*;

/**
 * @description: TODO
 * @author: Fannsy
 * @date: 2022/9/3 20:13
 * @version: 1.0.0
 * @url:
 */


@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
@RpcService(group = "test1",version = "version1")
public class HelloServiceImpl implements HelloService {
    @Override
    public void sayHello() {
        System.out.println("hello !");
    }
}
