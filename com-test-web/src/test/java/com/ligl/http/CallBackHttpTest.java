package com.ligl.http;

import com.alibaba.fastjson.JSON;
import com.ligl.AbstractJunitTest;
import com.ligl.common.feignClient.CallBackService;
import com.ligl.common.feignClient.NotifyCashResultReq;
import com.ligl.common.feignClient.NotifyResp;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Function:
 * Author: created by liguoliang
 * Date: 2017/9/27 0027 下午 3:14
 * Version: 1.0
 */
public class CallBackHttpTest extends AbstractJunitTest {

    public static final Logger logger = LoggerFactory.getLogger(CallBackHttpTest.class);
    @Autowired
    private CallBackService callBackService;

    @Test
    public void httpCallRequest() {
        NotifyCashResultReq req = new NotifyCashResultReq();
        logger.info("notifyCashResult请求参数：{}", req);
        NotifyResp resultResp = callBackService.notifyCashResult(req);
        logger.info("notifyCashResult返回的结果为:{}", JSON.toJSONString(resultResp));

    }

}
