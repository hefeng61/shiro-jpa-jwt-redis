package com.example.demo.config.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    @Autowired
    RedisTemplate<String, Object> template;

    /**
     * 获取指定key的值
     *
     * @param key
     * @return
     */
    public Object get(String key) {
        return template.opsForValue().get(key);
    }

    /**
     * 放入指定的key-value键值对
     *
     * @param key
     * @param value
     */
    public void set(String key, Object value,Long expire) {
//        template.opsForValue().set(key,value,expire);
        template.opsForValue().set(key,value,expire,TimeUnit.MILLISECONDS);
    }

    public void set(String key,Object value){
        template.opsForValue().set(key,value);
    }
    /**
     * 设置指定key的过期时间
     *
     * @param key
     * @param expireTime
     */
    public void setExpire(String key, Long expireTime) {
        template.expire(key, expireTime, TimeUnit.SECONDS);
    }

    /**
     * 获取指定key的过期时间
     *
     * @param key
     * @return
     */
    public long getExpire(String key) {
        return template.getExpire(key);
    }

    /**
     * 判断指定key是否存在
     * @param key
     * @return
     */
    public boolean hasKey(String key){
        return template.hasKey(key);
    }

    public void delete(String key){
        template.delete(key);
    }

}
