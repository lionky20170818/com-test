package com.ligl.common.aspect;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.aspectj.lang.JoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by liguoliang on 2017/1/3.
 * 使用springAop打印请求参数和返回参数及异常信息
 */
public class LogAspect {

    private static Logger logger = LoggerFactory.getLogger(LogAspect.class);

    //任何通知方法都可以将第一个参数定义为 org.aspectj.lang.JoinPoint类型
    public void before(JoinPoint call) {
        //获取目标对象对应的类名
        String className = call.getTarget().getClass().getName();
        //获取目标对象上正在执行的方法名
        String methodName = call.getSignature().getName();
        logger.info(className + "." + methodName + "开始执行");
        //获取参数
        Object[] args = call.getArgs();
        //拼接字符串,打印请求参数
        StringBuilder sb = new StringBuilder();
        sb.append("请求参数:【");
        for (int i = 0; i < args.length; i++) {
            //String argString = JsonUtils.getArgsString(args[i]);
            String argString = JSON.toJSONString(args[i], SerializerFeature.WriteDateUseDateFormat);
            sb.append(argString);
            if (i < args.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("】");
        logger.info(sb.toString());
    }

    public void afterReturn(JoinPoint call, Object retValue) {
        //获取目标对象对应的类名
        String className = call.getTarget().getClass().getName();
        //获取目标对象上正在执行的方法名
        String methodName = call.getSignature().getName();
        StringBuilder sb = new StringBuilder();
        sb.append(className).append(".").append(methodName);
        sb.append("返回值:【");
        //sb.append(JsonUtils.getArgsString(retValue));
        sb.append(JSON.toJSONString(retValue, SerializerFeature.WriteDateUseDateFormat));
        sb.append("】");
        logger.info(sb.toString());
    }

    public void afterThrowing(JoinPoint call, Throwable ex) {
        //获取目标对象对应的类名
        String className = call.getTarget().getClass().getName();
        //获取目标对象上正在执行的方法名
        String methodName = call.getSignature().getName();
        logger.error(className + "." + methodName + "发生异常:", ex);
    }


}
