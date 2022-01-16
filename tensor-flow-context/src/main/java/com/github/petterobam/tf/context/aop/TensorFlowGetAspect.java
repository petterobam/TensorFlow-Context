package com.github.petterobam.tf.context.aop;

import com.github.petterobam.tf.context.annotaion.TensorFlowGet;
import com.github.petterobam.tf.context.util.ClassReflectUtil;
import com.github.petterobam.tf.context.util.SpringContextUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 注解了 @TensorFlow 类里面的 Get 方法拦截
 *
 * @author 欧阳洁
 * @date 2021/12/23 16:33
 */
@Aspect
public class TensorFlowGetAspect {
    private static final Logger logger = LoggerFactory.getLogger(TensorFlowGetAspect.class);

    private static final String GET_METHOD_PREFIX = "get";
    private static final String SET_METHOD_PREFIX = "set";
    private static final String PROXY_CLASS_FILTER = "$";
    private static final String THIS_PARAM = "this";

    @SuppressWarnings("rawtypes")
    @Around("@within(com.github.petterobam.tf.context.annotaion.EnableTensorFlow) && execution(public * get*())")
    public Object handleRequest(ProceedingJoinPoint point) throws Throwable {
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        String methodName = method.getName();

        Object returnObj = fetchOriginExecute(point);
        if (null != returnObj) {
            logger.info("direct get {} method origin value", methodName);
            return returnObj;
        }

        if (null == methodName || !methodName.startsWith(GET_METHOD_PREFIX)) {
            return null;
        }

        // 获取 TensorFlowGet 注解，并找到执行服务和方法调用
        String filedName = getFiledName(methodName);
        Object context = point.getThis();
        Field field = getTensorFlowContextField(context.getClass(), filedName);
        if (field == null) {
            throw new IllegalArgumentException(filedName + " field is not Found！");
        }
        TensorFlowGet getConfig = field.getAnnotation(TensorFlowGet.class);
        if (null == getConfig) {
            return null;
        }
        String[] paramNames = getConfig.params();
        Class[] paramClassTypes = new Class[paramNames.length];
        Object[] params = new Object[paramNames.length];
        for (int i = 0; i < paramNames.length; i++) {
            if (THIS_PARAM.equals(paramNames[i])) {
                paramClassTypes[i] = getTensorFlowContextType(context.getClass());
                params[i] = context;
                continue;
            }
            String getParamName = getGetParamName(paramNames[i]);
            Field paramField = getTensorFlowContextField(context.getClass(), paramNames[i]);
            paramClassTypes[i] = paramField.getType();
            params[i] = ClassReflectUtil.invokeMethod(context, getParamName, null);
        }
        paramClassTypes = params.length == getConfig.paramTypes().length ? getConfig.paramTypes() : paramClassTypes;

        switch (getConfig.type()) {
            case Local:
                returnObj = ClassReflectUtil.invokeMethod(context, getConfig.methodName(), paramClassTypes, params);
                break;
            case Static:
                returnObj = ClassReflectUtil.invokeStaticMethod(getConfig.classType(), getConfig.methodName(), paramClassTypes, params);
                break;
            case Spring:
                Object service = getConfig.springName().length() > 0 ?
                        SpringContextUtil.getBean(getConfig.springName()) :
                        SpringContextUtil.getBean(getConfig.classType());
                returnObj = ClassReflectUtil.invokeMethod(service, getConfig.methodName(), paramClassTypes, params);
                break;
            default:
        }

        // 调用 set 赋值，阻断后续重复调用
        if (null != returnObj) {
            String setMethodName = getSetParamName(filedName);
            ClassReflectUtil.invokeMethod(context, setMethodName, new Class[]{field.getType()}, returnObj);
        }

        return returnObj;
    }

    private String getGetParamName(String paramName) {
        return GET_METHOD_PREFIX + paramName.substring(0, 1).toUpperCase() + paramName.substring(1);
    }

    private String getSetParamName(String paramName) {
        return SET_METHOD_PREFIX + paramName.substring(0, 1).toUpperCase() + paramName.substring(1);
    }

    private String getFiledName(String getMethodName) {
        return getMethodName.substring(3, 4).toLowerCase() + getMethodName.substring(4);
    }

    private Object fetchOriginExecute(ProceedingJoinPoint point) throws Throwable {
        Object returnObj;
        // 业务处理
        try {
            returnObj = point.proceed();
        } catch (Exception e) {
            logger.error("TensorFlowGetAspect，原始函数执行异常。", e);
            throw e;
        }
        return returnObj;
    }

    private Field getTensorFlowContextField(Class<?> cls, String fieldName) throws NoSuchFieldException {
        if (null == cls) {
            throw new IllegalArgumentException("class is null, can find field " + fieldName);
        }

        // 是否代理类
        if (cls.getName().contains(PROXY_CLASS_FILTER)) {
            return cls.getSuperclass().getDeclaredField(fieldName);
        }
        return cls.getDeclaredField(fieldName);
    }

    private Class getTensorFlowContextType(Class<?> cls) {
        if (null == cls) {
            throw new IllegalArgumentException("class is null, can find type");
        }

        // 是否代理类
        if (cls.getName().contains(PROXY_CLASS_FILTER)) {
            return cls.getSuperclass();
        }
        return cls;
    }
}
