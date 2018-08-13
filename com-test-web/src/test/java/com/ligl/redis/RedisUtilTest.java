package com.ligl.redis;

import com.ligl.AbstractJunitTest;
import com.ligl.common.redis.StringRedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Function:
 * Author: created by liguoliang
 * Date: 2017/12/15 0015 下午 3:29
 * Version: 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/applicationContext*.xml")
public class RedisUtilTest extends AbstractJunitTest {

    @Autowired
    private StringRedisUtil stringRedisUtil;

    @Test
    public void redisUtilTest1(){
        System.out.println("name20171215==="+stringRedisUtil.get("name20171215"));

    }
}
