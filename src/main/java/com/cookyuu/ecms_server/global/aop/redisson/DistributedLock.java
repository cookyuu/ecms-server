package com.cookyuu.ecms_server.global.aop.redisson;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    String key();
    long waitTime() default 3L;
    long leaseTime() default 5L;
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
