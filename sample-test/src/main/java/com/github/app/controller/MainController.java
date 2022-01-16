package com.github.app.controller;

import com.github.petterobam.tf.context.example.TfGetExampleContext;
import com.github.petterobam.tf.context.util.TensorFlowUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class MainController {
    /**
     * 主页
     *
     * @return
     */
    @RequestMapping("/test/tf/context")
    @ResponseBody
    public Object testTfContext(Integer origin1, Integer origin2) throws InstantiationException, IllegalAccessException {
        Map<String, Object> res = new HashMap<>();
        TfGetExampleContext context = TensorFlowUtil.fetchTensorFlowContext(TfGetExampleContext.class);
        context.setOrigin1(origin1);
        context.setOrigin2(origin2);
        Map<String, Object> res1 = new HashMap<>();
        res1.put("five", context.getFive());
        res1.put("first", context.getFirst());
        res1.put("second", context.getSecond());
        res1.put("third", context.getThird());
        res1.put("seven", context.getSeven());
        res1.put("fourth", context.getForth());
        res1.put("six", context.getSix());
        res1.put("originDatas", context.getOriginDatas());
        res.put("data1", res1);

        TfGetExampleContext context2 = TensorFlowUtil.fetchTensorFlowContext(TfGetExampleContext.class);
        context2.setOrigin2(origin1);
        context2.setOrigin1(origin2);
        Map<String, Object> res2 = new HashMap<>();
        res2.put("five", context2.getFive());
        res2.put("first", context2.getFirst());
        res2.put("second", context2.getSecond());
        res2.put("third", context2.getThird());
        res2.put("seven", context2.getSeven());
        res2.put("fourth", context2.getForth());
        res2.put("six", context2.getSix());
        res2.put("originDatas", context2.getOriginDatas());
        res.put("data2", res2);
        return res;
    }
}
