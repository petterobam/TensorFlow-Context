package com.github.petterobam.tf.context.example;

import com.github.petterobam.tf.context.annotaion.EnableTensorFlow;
import com.github.petterobam.tf.context.annotaion.TensorFlowGet;
import com.github.petterobam.tf.context.annotaion.enums.ServiceType;

import java.util.Map;

/**
 * 简单图计算例子
 * @author 欧阳洁
 * @date 2021/12/23 17:11
 */
@EnableTensorFlow
public class TfGetExampleContext {
    private Integer origin1 = 10;
    private Integer origin2 = 20;
    @TensorFlowGet(type = ServiceType.Local, methodName = "add", params = {"origin1", "origin2"})
    private Integer first;
    @TensorFlowGet(type = ServiceType.Local, methodName = "add", params = {"first", "origin2"})
    private Integer second;
    @TensorFlowGet(type = ServiceType.Local, methodName = "sub", params = {"origin1", "origin2"})
    private Integer third;
    @TensorFlowGet(type = ServiceType.Static, classType = Math.class, methodName = "negateExact", paramTypes = {int.class}, params = {"third"})
    private Integer forth;
    @TensorFlowGet(type = ServiceType.Local, methodName = "add", params = {"second", "forth"})
    private Integer five;
    @TensorFlowGet(type = ServiceType.Spring, classType = TfGetExampleService.class, methodName = "calculate1", params = {"third"})
    private Integer six;
    @TensorFlowGet(type = ServiceType.Spring, springName = "tfGetExampleService", methodName = "calculate2", params = {"third"})
    private Integer seven;
    @TensorFlowGet(type = ServiceType.Spring, springName = "tfGetExampleService", methodName = "fetchOrigins", params = {"this"})
    private Map<String, Object> originDatas;

    public Integer getOrigin1() {
        return origin1;
    }

    public void setOrigin1(Integer origin1) {
        this.origin1 = origin1;
    }

    public Integer getOrigin2() {
        return origin2;
    }

    public void setOrigin2(Integer origin2) {
        this.origin2 = origin2;
    }

    public Integer getFirst() {
        return first;
    }

    public void setFirst(Integer first) {
        this.first = first;
    }

    public Integer getSecond() {
        return second;
    }

    public void setSecond(Integer second) {
        this.second = second;
    }

    public Integer getThird() {
        return third;
    }

    public void setThird(Integer third) {
        this.third = third;
    }

    public Integer getForth() {
        return forth;
    }

    public void setForth(Integer forth) {
        this.forth = forth;
    }

    public Integer getFive() {
        return five;
    }

    public void setFive(Integer five) {
        this.five = five;
    }

    public Integer getSix() {
        return six;
    }

    public void setSix(Integer six) {
        this.six = six;
    }

    public Integer getSeven() {
        return seven;
    }

    public void setSeven(Integer seven) {
        this.seven = seven;
    }

    public Map<String, Object> getOriginDatas() {
        return originDatas;
    }

    public void setOriginDatas(Map<String, Object> originDatas) {
        this.originDatas = originDatas;
    }

    public Integer add(Integer a, Integer b) {
        return (a == null ? 0 : a) + (b == null ? 0 : b);
    }

    public Integer sub(Integer c, Integer b) {
        return (c == null ? 0 : c) - (b == null ? 0 : b);
    }
}
