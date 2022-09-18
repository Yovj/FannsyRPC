package com.yf.spring.config;

import com.yf.common.factory.SingletonFactory;
import com.yf.config.RpcServiceConfig;
import com.yf.proxy.RpcClientProxy;
import com.yf.service.HelloService;
import com.yf.spring.annotaion.RpcReference;
import com.yf.spring.annotaion.RpcService;
import com.yf.transport.RpcRequestTransport;
import com.yf.transport.client.NettyRpcClient;
import com.yf.transport.server.NettyRpcServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * description: TODO
 * author: ConquerJ.
 * date: 2022/9/18 8:28
 * url:
 */
@Slf4j
public class RpcBeanProcessor implements ApplicationContextAware, BeanPostProcessor {
    private ApplicationContext applicationContext;

    @Resource
    private NettyRpcServer server;

    @Resource
    private NettyRpcClient client;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 如果 bean 是 RpcService，将服务发布出去
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            log.info("[{}] is annotated with  [{}]", bean.getClass().getName(), RpcService.class.getCanonicalName());
            // get RpcService annotation
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            // build RpcServiceProperties
            RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                    .group(rpcService.group())
                    .version(rpcService.version())
                    .token(rpcService.serviceToken())
                    .permitIps(new HashSet<>(Arrays.asList(rpcService.permitIps())))
                    .service(bean).build();
            server.registerService(rpcServiceConfig);
        } else {
            // 如果 bean 中包含 RpcReference 注解的字段，修改其引用，指向代理对象
            Field[] declaredFields = bean.getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(RpcReference.class)){


                    RpcReference rpcReference = field.getAnnotation(RpcReference.class);
                    try {
                        field.setAccessible(true);
                        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
                        rpcServiceConfig.setGroup(rpcReference.group());
                        rpcServiceConfig.setVersion(rpcReference.version());
                        rpcServiceConfig.setToken(rpcReference.serviceToken());
                        rpcServiceConfig.setDirectIp(Arrays.asList(rpcReference.url()));


                        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcServiceConfig,client);
                        Object proxy = rpcClientProxy.getProxy(field.getType());

                        field.set(bean,proxy);

                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }

                }

            }


        }
        return bean;
    }
}
