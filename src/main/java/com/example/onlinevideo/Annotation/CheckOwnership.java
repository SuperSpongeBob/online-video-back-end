package com.example.onlinevideo.Annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//  自定义注解，用于标记需要限流的方法

@Target(ElementType.METHOD)             //  注解作用在方法上
@Retention(RetentionPolicy.RUNTIME)     //  运行时生效
public @interface CheckOwnership {
    /**
     * SpEL 表达式，用于动态解析目标用户ID
     * 例如：`#videoAlbum.userId`、`#userId`
     */
    String expression();
    /**
     * 是否允许管理员跳过校验（默认允许）
     */
    boolean allowAdmin() default true;
}
