package com.yf.spring.annotaion;


import org.springframework.stereotype.Component;

import java.lang.annotation.*;
import java.util.ArrayList;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcReference {

    String[] url() default {};

    String group() default "default";

    String serviceToken() default "";

    int timeOut() default 3000;

    int retry() default 1;

    boolean async() default false;

    /**
     * Service version, default value is empty string
     */
    String version() default "";
}
