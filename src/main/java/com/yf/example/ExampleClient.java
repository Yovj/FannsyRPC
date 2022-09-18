package com.yf.example;

import com.yf.service.HelloService;
import com.yf.spring.annotaion.RpcReference;
import org.springframework.stereotype.Component;

/**
 * description: TODO
 * author: ConquerJ.
 * date: 2022/9/18 10:03
 * url:
 */
@Component
public class ExampleClient {

    @RpcReference(group = "test1",version = "version1")
    private HelloService helloService;


    public void testClient(){
        helloService.sayHello();
    }


}
