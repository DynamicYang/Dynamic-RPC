package org.dynamic.rpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author: DynamicYang
 * @create: 2024-03-08
 * @Description:
 */

@Target(ElementType.TYPE)
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface TryTimes {
    int tryTimes() default 3;
    int intervalTime() default 2000;
}
