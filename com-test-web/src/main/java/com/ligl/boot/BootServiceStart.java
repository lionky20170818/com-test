package com.ligl.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ImportResource({"classpath*:spring/applicationContext*.xml"})
@PropertySource(value = {"file:${ENV_CONF_PATH}/application.properties"}, ignoreResourceNotFound = true)
public class BootServiceStart implements EmbeddedServletContainerCustomizer {

    private final static Logger logger = LoggerFactory.getLogger(BootServiceStart.class);

    @Value("${front.http.port}")
    private int port;

    public static void main(String[] args) throws InterruptedException {
//        SpringApplication.run(BootServiceStart.class, args);
        ApplicationContext ctx = new SpringApplicationBuilder()
                .sources(BootServiceStart.class)
                .web(true)  //开启web服务
//                .bannerMode(Banner.Mode.OFF)  //关闭Banner
                .run(args);
        logger.info("项目启动!");
    }


    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {
        container.setPort(port);//指定web端口
    }

//	@Bean
//	public com.ligl.test2.com.trace.ZbHttpFilter ZbHttpFilter(){
//		
//		return new com.ligl.test2.com.trace.ZbHttpFilter();
//		
//	}
}
