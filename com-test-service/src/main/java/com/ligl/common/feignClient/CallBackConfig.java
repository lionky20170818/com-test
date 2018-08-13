package com.ligl.common.feignClient;

import feign.Feign;
import feign.Logger;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置回调bean
 * Created by liguoliang on 2017/8/29.
 */
@Configuration
public class CallBackConfig {
    @Value("${application.cnode.url}")
    String host;

//    @Value("${aes.decode:true}")
//    boolean decode;
//    @Value("${aes.key:9999}")
//    String key;
//    AesRequestIntercept aesRequestIntercept;
//    ResponseMapper responseMapper;
//    AesUtil aesEncoder;

//    @PostConstruct
//    public void initAesEncryptAndDecrypt() {
//        aesEncoder = new AesUtil();
//        aesEncoder.setKey(key);
//        aesRequestIntercept = new AesRequestIntercept(decode, aesEncoder);
//        responseMapper = new AesResponseHandler(decode, aesEncoder);
//    }

    @Bean
    public CallBackService newCallBackService() {
        return Feign.builder().encoder(new JacksonEncoder()).decoder(new JacksonDecoder())
                .logger(new Slf4jLogger()).logLevel(Logger.Level.FULL)
                .target(CallBackService.class, host);
    }

}
