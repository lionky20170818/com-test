package com.ligl.common.aspect;

import com.ligl.common.enums.ResultCodeEnum;
import com.ligl.common.exception.CommonException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;

import java.lang.reflect.Method;


/**
 * Created by liguoliang on 2017/4/20.
 * 使用springAop统一处理异常
 */
public class ExceptionAspect {

    private static Logger logger = LoggerFactory.getLogger(ExceptionAspect.class);

    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object result = null;
        Signature signature = proceedingJoinPoint.getSignature();
        Class returnType = ((MethodSignature) signature).getReturnType();
        try {
            result = proceedingJoinPoint.proceed();
        } catch (Exception e) {
            Object response = Class.forName(returnType.getName()).newInstance();
            Method[] methods = returnType.getMethods();
            Method setResultCodeMethod = null;
            Method setResultMsgMethod = null;
            for (Method method : methods) {
                String methodName = method.getName().substring(method.getName().lastIndexOf(".") + 1);
                if (methodName.equals("setResultCode")) {
                    setResultCodeMethod = method;
                }
                if (methodName.equals("setResultMsg")) {
                    setResultMsgMethod = method;
                }
            }
            if (e instanceof CommonException) {
                CommonException ex = (CommonException) e;
                setResultCodeMethod.invoke(response, ResultCodeEnum.PARAMS_VALIDATE_FAIL.code());
                setResultMsgMethod.invoke(response, ex.getMessage());
                logger.info(ex.getMessage());
            } else if (e instanceof DuplicateKeyException) {
                setResultCodeMethod.invoke(response, ResultCodeEnum.FAIL.code());
                setResultMsgMethod.invoke(response, "请求已处理");
            } else {
                setResultCodeMethod.invoke(response, ResultCodeEnum.FAIL.code());
                setResultMsgMethod.invoke(response, ResultCodeEnum.FAIL.desc());
                //获取目标对象对应的类名
                String className = proceedingJoinPoint.getTarget().getClass().getName();
                //获取目标对象上正在执行的方法名
                String methodName = proceedingJoinPoint.getSignature().getName();
                logger.error("{}在执行方法{}时抛出异常", className, methodName, e);
            }

            return response;
        }
        return result;
    }
}
