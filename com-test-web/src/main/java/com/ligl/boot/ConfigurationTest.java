package com.ligl.boot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Function:通过注解加载bean，在启动的时候会作为配置文件加载
 * Author: created by liguoliang
 * Date: 2017/10/24 0024 下午 4:35
 * Version: 1.0
 */
@Configuration
public class ConfigurationTest {
    //    @Value("com.mysql.jdbc.Driver")
//    private String driverClassName;
//
//    @Value("jdbc:mysql://192.168.0.76:3306/boss?useUnicode=true&characterEncoding=utf8")
//    private String driverUrl;
//
////    @Value("${root}")
//    @Value("root")
//    private String driverUsername;
//
//    @Value("qylc@123")
//    private String driverPassword;
    @Value("${aes.decode:true}")
    String decode;

//    @Bean
//    @Scope("prototype")
//    public FilterRegistrationBean getFilterTest111() {
//        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
//        registrationBean.setFilter(new FincoreHttpAesFilter());
//        Map<String, String> initParam = new HashMap<String, String>();
//        initParam.put("decode", decode);
//        registrationBean.setInitParameters(initParam);
//        registrationBean.setUrlPatterns(Arrays.asList("/*"));
//        registrationBean.setDispatcherTypes(DispatcherType.FORWARD, DispatcherType.REQUEST);
//        System.out.println("getFilterTest111===");
//        return registrationBean;
//    }
}
