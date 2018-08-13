package com.ligl.redis;

import com.ligl.common.redis.RedisManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Function:
 * Author: created by liguoliang
 * Date: 2017/8/23 0023 下午 3:14
 * Version: 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/applicationContext*.xml")
public class RedisManageTest {
    private static Logger logger = LoggerFactory.getLogger(RedisManageTest.class);

    @Autowired
    protected RedisManager redisManager;

    @Test
    public void redisTimeOut() {
        int timeout = 30;
        redisManager.set("name20171215", "zb", timeout);
        //logger.info(redisCache.setKV("name", "zb", timeout));
        logger.info("==="+redisManager.get("name20171215"));
        try {
            Thread.sleep(timeout * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("==="+redisManager.get("name"));
    }

}
