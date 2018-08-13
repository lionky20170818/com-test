package com.ligl.redis;

import com.ligl.common.redisTemplate.IUserDao;
import com.ligl.common.redisTemplate.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.websocket.Session;

/**
 * Function:spring-redisTemplate.xml配置文件不一样
 * Author: created by liguoliang
 * Date: 2017/8/23 0023 下午 3:56
 * Version: 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/applicationContext*.xml")
@ActiveProfiles("dev")
public class RedisTemplateTest {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private IUserDao userDao;

    //ValueOperations方式,可以参考http://blog.csdn.net/whatlookingfor/article/details/51863286
    @Resource(name = "redisTemplate")
    private RedisTemplate<String, String> template;
    @Resource(name = "redisTemplate")
    private ValueOperations<String, Object> vOps;

    public static void main(String[] args) {
        System.out.println("111=====");
//        new RedisTemplateTest().testAddUser();
    }

    @Test
    public void testAddTem() {
        //调用方法
        template.opsForValue().set("key20171128","value20171128");
        System.out.println("RedisTemplateTest.testAddTem=="+template.opsForValue().get("key20171128"));
        //调用方法
        vOps.set("wohenhao20171128","nihaobuhao20171128");
        System.out.println("vOps.testAddTem=="+vOps.get("wohenhao20171128"));


    }

    /**
     * 新增
     * <br>------------------------------<br>
     */
    @Test
    public void testAddUser() {
        ThreadLocal session = new ThreadLocal();
        Session test = (Session) session.get();

        User user = new User();
        user.setId("user20171125");
        user.setName("nihaoma");
        user.setPassword("wohenhao");
        boolean result = userDao.add(user);
        Assert.assertTrue(result);
    }

    /**
     * 修改
     * <br>------------------------------<br>
     */
    @Test
    public void testUpdate() {
        User user = new User();
        user.setId("user1");
        user.setName("new_password");
        boolean result = userDao.update(user);
        Assert.assertTrue(result);
    }

    /**
     * 通过key删除单个
     * <br>------------------------------<br>
     */
    @Test
    public void testDelete() {
        String key = "user1";
        userDao.delete(key);
    }

    /**
     * 获取
     * <br>------------------------------<br>
     */
    @Test
    public void testGetUser() {
        String id = "user1";
        User user = userDao.get(id);
        Assert.assertNotNull(user);
        Assert.assertEquals(user.getName(), "java2000_wl");
    }
}
