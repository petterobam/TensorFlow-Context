package com.github.petterobam.tf.context.example;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 欧阳洁
 * @date 2021/12/25 15:58
 */
@Component
public class TfGetExampleService {
    public Integer calculate1(Integer val) {
        if (null == val) {
            return 0;
        }
        return val * val * val;
    }
    public Integer calculate2(Integer val) {
        if (null == val) {
            return 0;
        }
        return val + val + val;
    }

    public Map<String, Object> fetchOrigins(TfGetExampleContext context) {
        Map<String, Object> res = new HashMap<>();
        res.put("origin1", context.getOrigin1());
        res.put("origin2", context.getOrigin2());
        return res;
    }
}
