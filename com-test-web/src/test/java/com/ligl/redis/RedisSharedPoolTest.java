package com.ligl.redis;

import com.ligl.common.redis.RedisCache;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.aliyun.mns.client.impl.AbstractAction.logger;

/**
 * Created by liguoliang on 2017/1/9.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/applicationContext*.xml")
public class RedisSharedPoolTest {

    @Autowired
    private RedisCache redisCache;

    @Test
    public void set() {
        int timeout = 30;
        redisCache.setKV("name", "zb", timeout);
        //logger.info(redisCache.setKV("name", "zb", timeout));
        logger.info(redisCache.getV("name"));

        try {
            Thread.sleep(timeout * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info(redisCache.getV("name"));
    }
}
