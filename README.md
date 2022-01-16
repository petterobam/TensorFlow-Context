# TensorFlow-Context

TensorFlow上下文，简单基于注解JavaBean实现的图计算编排功能，可以将过程值存储，用于复杂计算或组装。

名为【仿TensorFlow懒执行编程和注解自编排】，在我的个人代办里面。
依稀记得是很久前学习 TensorFlow 的一个想法，年尾翻到，简要 finish 一下这个 idea，属于一种临时简要的探索实践。

## 背景

PS：[什么是 TensorFlow？](https://tensorflow.google.cn/learn?hl=zh-cn)

TensorFlow 里面有个图计算，先编排好计算流程，最后点执行直接渲染节点数据。我觉得这种功能可以用 Java 实现，于是就简单写了这个应用。

下面我们来简单看下图计算模型是怎么样的渲染流程：

> 如下有计算模型

![计算模型](https://p6.music.126.net/obj/wonDlsKUwrLClGjCm8Kx/12335286877/e538/3808/de63/9d9bbe5efe132a20b6e26f7019f19b30.png)

> ### 获取 five 的值

```
getFive 的时候，会回溯计算 five 需要的值 second 和 fourth，
发现 second 的值需要通过 first、origin2 计算，origin2 是已知的值，
然后需要计算 first = origin1 + origin2，然后 second = first + origin2
依次回溯递推，依次将 first、second、third、fourth、five 全部计算出来，并赋值。
```

![getFive](https://p5.music.126.net/obj/wonDlsKUwrLClGjCm8Kx/12335286883/08c8/6cd1/27ec/582cd32d1e65925e4b70bbe3184310b2.png)

---

通过 getFive 的动作，我们发现原来的模型被渲染成了下面这样。

![getFiveFinish](https://p6.music.126.net/obj/wonDlsKUwrLClGjCm8Kx/12335289339/0dfa/2eeb/8947/798876c93c9c78e9c5eb0f9b46bbe258.png)

---

当节点被计算赋值后（变成绿色的那种）就可以直接被使用，如上，再 getSix 时候，直接可以根据 third 的值进行计算。

```
PS： 那这种模型是否可用于业务代码呢？
1、复杂业务代码组装（过程变量可复用，自动化执行需要的步骤）
2、构建业务上下文，用于复杂业务处理支撑
3、对外提供简化版业务上下文，调用方按需直接获取，依赖按需加载，降低编码复杂度
```

## Java 实现并应用

本来是想模拟 lombok 实现静态代码生成注入，这样可读性和性能会更好，不过先简单实现，后续有时间再研究和优化。

代码只有一两百行，感兴趣参见： <https://github.com/petterobam/TensorFlow-Context>

> ### 依赖

```xml
<dependency>
    <groupId>com.github.petterobam</groupId>
    <artifactId>tensor-flow-context</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

> ### 定义计算模型

```java
PS：计算模型定义需要在 Spring 扫包范围

@Data
@EnableTensorFlow
public class TfGetExampleContext {
    private Integer origin1 = 10;
    private Integer origin2 = 20;
    /**
     * 本地方法
     */
    @TensorFlowGet(type = ServiceType.Local, methodName = "add", params = {"origin1", "origin2"})
    private Integer first;
    @TensorFlowGet(type = ServiceType.Local, methodName = "add", params = {"first", "origin2"})
    private Integer second;
    @TensorFlowGet(type = ServiceType.Local, methodName = "sub", params = {"origin1", "origin2"})
    private Integer third;
    /**
     * 静态方法
     */
    @TensorFlowGet(type = ServiceType.Static, classType = Math.class, methodName = "negateExact", paramTypes = {int.class}, params = {"third"})
    private Integer forth;

    @TensorFlowGet(type = ServiceType.Local, methodName = "add", params = {"second", "forth"})
    private Integer five;
    /**
     * Spring Bean 的方法
     */
    @TensorFlowGet(type = ServiceType.Spring, classType = TfGetExampleService.class, methodName = "calculate1", params = {"third"})
    private Integer six;
    @TensorFlowGet(type = ServiceType.Spring, springName = "tfGetExampleService", methodName = "calculate2", params = {"third"})
    private Integer seven;
    /**
     * 自身入参
     */
    @TensorFlowGet(type = ServiceType.Spring, springName = "tfGetExampleService", methodName = "fetchOrigins", params = {"this"})
    private Map<String, Object> originDatas;

    public Integer add(Integer a, Integer b) {
        return (a == null ? 0 : a) + (b == null ? 0 : b);
    }

    public Integer sub(Integer c, Integer b) {
        return (c == null ? 0 : c) - (b == null ? 0 : b);
    }
}

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
```

> ### 如何使用？ 

示例： [http://127.0.0.1:7600/test/tf/context?origin1=87&origin2=23](http://127.0.0.1:7600/test/tf/context?origin1=87&origin2=23)

```java
@RequestMapping("/test/tf/context")
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
    log.info("TensorFlowGet for Context finish！");
    return res;
}
```

> ### 此模型两种传参，计算结果如下

```json
{
    "data2": {
        "six": 262144,
        "third": 64,
        "originDatas": {
            "origin2": 23,
            "origin1": 87
        },
        "seven": 192,
        "fourth": -64,
        "five": 69,
        "first": 110,
        "second": 133
    },
    "data1": {
        "six": -262144,
        "third": -64,
        "originDatas": {
            "origin2": 87,
            "origin1": 23
        },
        "seven": -192,
        "fourth": 64,
        "five": 261,
        "first": 110,
        "second": 197
    }
}
```

> 示例 - data 1

![示例1](https://p5.music.126.net/obj/wonDlsKUwrLClGjCm8Kx/12344870473/0ce2/f2f5/b735/f8dd5fc599aa91b5f09e5214e69caa43.png)

> 示例 - data 2

![示例2](https://p6.music.126.net/obj/wonDlsKUwrLClGjCm8Kx/12344879560/dbbd/8aa9/764d/62baa856e7df65bddad0b9425ecdb39b.png)