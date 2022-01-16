package com.github.petterobam.tf.context.util;

import com.github.petterobam.tf.context.annotaion.EnableTensorFlow;

/**
 * TensorFlow 工具
 *
 * @author 欧阳洁
 * @date 2021/12/24 18:42
 */
public class TensorFlowUtil {
    /**
     * 获取 TensorFlow Context
     *
     * @param clazz 类
     * @param <T>   类型
     * @return 对象
     * @throws IllegalAccessException 访问异常
     * @throws InstantiationException 实例化异常
     */
    public static <T> T fetchTensorFlowContext(Class<T> clazz) throws IllegalAccessException, InstantiationException {
        if (null == clazz) {
            return null;
        }

        if (null == clazz.getDeclaredAnnotation(EnableTensorFlow.class)) {
            return clazz.newInstance();
        }

        return SpringContextUtil.getBean(clazz);
    }
}
