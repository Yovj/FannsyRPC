package com.yf.spring.annotaion;


import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface RpcService {
    int limit() default 0;

    String group() default "default";

    String serviceToken() default "";

    /**
     * Service version, default value is empty string
     */
    String version() default "";

    String[] permitIps() default {};


}
