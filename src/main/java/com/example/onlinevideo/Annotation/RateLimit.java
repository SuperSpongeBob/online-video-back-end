package com.example.onlinevideo.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//  自定义注解，用于标记需要限流的方法

@Target(ElementType.METHOD)             //  注解作用在方法上
@Retention(RetentionPolicy.RUNTIME)     //  运行时生效
public @interface RateLimit {
    int maxRequests() default 60;       //  最大请求次数
    int timeWindow() default 60;        //  时间窗口
    String key() default "";            //  限流键（用户电话、IP地址、User-Agent）
}
