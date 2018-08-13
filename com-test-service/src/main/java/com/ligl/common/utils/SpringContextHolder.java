package com.ligl.common.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringContextHolder implements ApplicationContextAware {

    public static ApplicationContext context;

    public static Object getBean(Class clazz) {
        return context.getBean(clazz);
    }

    public static Object getBean(String className) {
        return context.getBean(className);
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        SpringContextHolder.context = context;
    }
}