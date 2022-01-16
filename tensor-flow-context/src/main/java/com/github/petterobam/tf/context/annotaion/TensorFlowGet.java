package com.github.petterobam.tf.context.annotaion;

import com.github.petterobam.tf.context.annotaion.enums.ServiceType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TensorFlow 注解
 *
 * @author 欧阳洁
 * @date 2021/12/23 16:13
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface TensorFlowGet {
    /**
     * 方法服务的类型
     */
    ServiceType type() default ServiceType.Spring;

    /**
     * 类名 （Spring 或 Static 模式传入）
     */
    Class classType() default Object.class;

    /**
     * spring beanName（Spring 模式适用）
     */
    String springName() default "";

    /**
     * 方法名
     */
    String methodName();

    /**
     * 参数类型
     */
    Class[] paramTypes() default {};

    /**
     * 参数名（适用的类中成员变量）
     */
    String[] params() default {};
}
