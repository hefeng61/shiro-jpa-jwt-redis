package com.example.demo.config.cache;


import com.example.demo.config.jwt.JwtUtil;
import com.example.demo.config.redis.RedisUtil;
import com.example.demo.constant.WebConstant;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.Set;

/**
 * 使用redis替代原来的缓存方式
 *
 * @param <K>
 * @param <V>
 */
public class CustomCache<K, V> implements Cache<K, V> {

//    private RedisTemplate<String, Object> redisTemplate;

    private String shiroExpireTime = "60000";

//    public CustomCache(RedisTemplate<String, Object> redisTemplate) {
//        this.redisTemplate = redisTemplate;
//    }

    private RedisUtil redisUtil;

    public CustomCache(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    private String getKey(Object key) {
        System.out.println(WebConstant.SHIRO_CACHE + JwtUtil.getClaim(key.toString(),WebConstant.USER_ACCOUNT));
        return WebConstant.SHIRO_CACHE + JwtUtil.getClaim(key.toString(),WebConstant.USER_ACCOUNT);
    }

    @Override
    public Object get(Object key) throws CacheException {
//        return redisTemplate.opsForValue().get(this.getKey(key));
        return redisUtil.get(this.getKey(key));
    }

    @Override
    public Object put(Object key, Object value) throws CacheException {
//        redisTemplate.opsForValue().set(this.getKey(key), value, Long.parseLong(shiroExpireTime));
        redisUtil.set(this.getKey(key),value,Long.parseLong(shiroExpireTime));
//        redisUtil.set(this.getKey(key),value);
//        redisUtil.setExpire(this.getKey(key),Long.parseLong(shiroExpireTime));
        return true;
    }

    @Override
    public Object remove(Object key) throws CacheException {
//        redisTemplate.delete(this.getKey(key));
        redisUtil.delete(this.getKey(key));
        return null;
    }

    @Override
    public void clear() throws CacheException {

    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Set<K> keys() {
        return null;
    }

    @Override
    public Collection<V> values() {
        return null;
    }
}
