package com.github.petterobam.tf.context.annotaion.enums;

/**
 * @author 欧阳洁
 * @date 2021/9/9 16:07
 */
public enum ServiceType {
    /**
     * 本地方法
     */
    Local,
    /**
     * spring 容器 bean
     */
    Spring,
    /**
     * 静态类对应方法
     */
    Static
}
