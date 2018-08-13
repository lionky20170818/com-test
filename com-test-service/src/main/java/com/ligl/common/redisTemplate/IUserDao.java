package com.ligl.common.redisTemplate;

/**
 * Function:
 * Author: created by liguoliang
 * Date: 2017/7/20 0020 上午 10:36
 * Version: 1.0
 */
public interface IUserDao {

    /**
     * 新增
     * <br>------------------------------<br>
     *
     * @param user
     * @return
     */
    boolean add(User user);

    /**
     * 删除
     * <br>------------------------------<br>
     *
     * @param key
     */
    void delete(String key);

    /**
     * 修改
     * <br>------------------------------<br>
     *
     * @param user
     * @return
     */
    boolean update(User user);

    /**
     * 通过key获取
     * <br>------------------------------<br>
     *
     * @param keyId
     * @return
     */
    User get(String keyId);
}
