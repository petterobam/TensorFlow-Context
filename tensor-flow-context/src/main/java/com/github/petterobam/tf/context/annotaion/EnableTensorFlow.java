package com.github.petterobam.tf.context.annotaion;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TensorFlow 注解（原型模式，每次获取都会重新创建）
 *
 * @author 欧阳洁
 * @date 2021/12/23 16:13
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component
public @interface EnableTensorFlow {

}
