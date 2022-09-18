package com.yf;

import com.yf.transport.server.NettyRpcServer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Resource;

/**
 * description: TODO
 * author: ConquerJ.
 * date: 2022/9/18 9:48
 * url:
 */

@SpringBootApplication
public class FannsyRPCApplication implements CommandLineRunner {
    @Resource
    NettyRpcServer server;

    public static void main(String[] args) {
        SpringApplication.run(FannsyRPCApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
//        server.start();
    }
}
