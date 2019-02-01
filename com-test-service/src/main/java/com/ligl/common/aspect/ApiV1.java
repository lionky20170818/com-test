package com.ligl.common.aspect;

/**
 * Author: created by liguoliang
 * Date: 2019/2/1 11:50 50
 * Version: 1.0
 * Function: 用于
 */
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RestController
@RequestMapping({"/api-v1"})
public @interface ApiV1 {
    String PATH = "/api-v1";
}
