package com.github.petterobam.tf.context.auto;

import com.github.petterobam.tf.context.aop.TensorFlowGetAspect;
import com.github.petterobam.tf.context.example.TfGetExampleContext;
import com.github.petterobam.tf.context.example.TfGetExampleService;
import com.github.petterobam.tf.context.util.SpringContextUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import({TfGetExampleContext.class})
public class AutoConfig {
    @Bean
    public SpringContextUtil springContextUtil() {
        return new SpringContextUtil();
    }

    @Bean
    public TensorFlowGetAspect tensorFlowGetAspect() {
        return new TensorFlowGetAspect();
    }

    @Bean
    public TfGetExampleService tfGetExampleService() {
        return new TfGetExampleService();
    }
}
