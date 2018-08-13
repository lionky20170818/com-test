package com.ligl.common.redisTemplate;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Function:
 * Author: created by liguoliang
 * Date: 2017/7/20 0020 上午 10:29
 * Version: 1.0
 */
@Repository
public class UserDaoImpl extends AbstractBaseRedisService<String, User> implements IUserDao {
    /**
     * 新增
     * <br>------------------------------<br>
     *
     * @param user
     * @return
     */
    @Override
    public boolean add(final User user) {
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection)
                    throws DataAccessException {
                RedisSerializer<String> serializer = getRedisSerializer();
                byte[] key = serializer.serialize(user.getId());
                byte[] name = serializer.serialize(JsonUtils.object2Json(user));
                return connection.setNX(key, name);
            }
        });
        return result;
    }

    /**
     * 删除
     * <br>------------------------------<br>
     *
     * @param key
     */
    @Override
    public void delete(String key) {
        List<String> list = new ArrayList<String>();
        list.add(key);
        delete(list);
    }

    /**
     * 删除多个
     * <br>------------------------------<br>
     *
     * @param keys
     */
    public void delete(List<String> keys) {
        redisTemplate.delete(keys);
    }

    /**
     * 修改
     * <br>------------------------------<br>
     *
     * @param user
     * @return
     */
    @Override
    public boolean update(final User user) {
        String key = user.getId();
        if (get(key) == null) {
            throw new NullPointerException("数据行不存在, key = " + key);
        }
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection)
                    throws DataAccessException {
                RedisSerializer<String> serializer = getRedisSerializer();
                byte[] key = serializer.serialize(user.getId());
                byte[] name = serializer.serialize(user.getName());
                connection.set(key, name);
                return true;
            }
        });
        return result;
    }

    /**
     * 通过key获取
     * <br>------------------------------<br>
     *
     * @param keyId
     * @return
     */
    @Override
    public User get(final String keyId) {
        User result = redisTemplate.execute(new RedisCallback<User>() {
            public User doInRedis(RedisConnection connection)
                    throws DataAccessException {
                RedisSerializer<String> serializer = getRedisSerializer();
                byte[] key = serializer.serialize(keyId);
                byte[] value = connection.get(key);
                if (value == null) {
                    return null;
                }
                String name = serializer.deserialize(value);
                return new User(keyId, name, null);
            }
        });
        return result;
    }
}
