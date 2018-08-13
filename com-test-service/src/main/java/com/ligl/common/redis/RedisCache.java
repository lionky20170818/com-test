package com.ligl.common.redis;


import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

public class RedisCache {
    private int DEFAULT_TIMEOUT = 1800;
    public String prefix_key = "com_test_sharedPool";
    private ShardedJedisPool jedisPool;

    public RedisCache(ShardedJedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public String getPrefix_key() {
        return this.prefix_key;
    }

    public void setPrefix_key(String prefix_key) {
        this.prefix_key = prefix_key;
    }

    public Result getV(String key) {
        Result result = new Result(0);
        ShardedJedis redis = null;
        if (StringUtils.isEmpty(key)) {
            result.setStatus(2);
            return result;
        } else {
            Result var5;
            try {
                redis = this.jedisPool.getResource();
                String e = redis.get(this.prefix_key + key);
                var5 = result.setValue(e);
                return var5;
            } catch (Exception var9) {
                var9.printStackTrace();
                result.setStatus(3);
                var5 = result;
            } finally {
                if (redis != null) {
                    redis.close();
                }

            }

            return var5;
        }
    }

    public Result setKV(String key, String value) {
        return this.setKV(key, value, this.DEFAULT_TIMEOUT);
    }

    public Result setKV(String key, String value, int timeout) {
        Result result = new Result(0);
        ShardedJedis redis = null;
        if (!StringUtils.isEmpty(key) && !StringUtils.isEmpty(value)) {
            Result var7;
            try {
                redis = this.jedisPool.getResource();
                if (timeout == -1) {
                    redis.set(this.prefix_key + key, value);
                } else {
                    redis.setex(this.prefix_key + key, timeout == 0 ? this.DEFAULT_TIMEOUT : timeout, value);
                }

                Result e = result;
                return e;
            } catch (Exception var11) {
                var11.printStackTrace();
                result.setStatus(3);
                var7 = result;
            } finally {
                if (redis != null) {
                    redis.close();
                }

            }

            return var7;
        } else {
            result.setStatus(2);
            return result;
        }
    }

    public Result hGetV(String field) {
        Result result = new Result(0);
        ShardedJedis redis = null;
        if (StringUtils.isEmpty(field)) {
            result.setStatus(2);
            return result;
        } else {
            Result var5;
            try {
                redis = this.jedisPool.getResource();
                String e = redis.hget(this.prefix_key, field);
                var5 = result.setValue(e);
                return var5;
            } catch (Exception var9) {
                var9.printStackTrace();
                result.setStatus(3);
                var5 = result;
            } finally {
                if (redis != null) {
                    redis.close();
                }

            }

            return var5;
        }
    }

    public Result hSetKV(String field, String value) {
        Result result = new Result(0);
        ShardedJedis redis = null;
        if (StringUtils.isEmpty(value)) {
            result.setStatus(2);
            return result;
        } else {
            Result var6;
            try {
                redis = this.jedisPool.getResource();
                redis.hset(this.prefix_key, field, value);
                Result e = result;
                return e;
            } catch (Exception var10) {
                var10.printStackTrace();
                result.setStatus(3);
                var6 = result;
            } finally {
                if (redis != null) {
                    redis.close();
                }

            }

            return var6;
        }
    }

    public Result delV(String key) {
        Result result = new Result(0);
        ShardedJedis redis = null;
        if (StringUtils.isEmpty(key)) {
            result.setStatus(2);
            return result;
        } else {
            Result var5;
            try {
                redis = this.jedisPool.getResource();
                Long e = redis.del(key);
                var5 = result.setValue(e);
                return var5;
            } catch (Exception var9) {
                var9.printStackTrace();
                result.setStatus(3);
                var5 = result;
            } finally {
                if (redis != null) {
                    redis.close();
                }

            }

            return var5;
        }
    }
}