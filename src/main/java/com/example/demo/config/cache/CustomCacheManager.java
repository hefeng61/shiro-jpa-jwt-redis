package com.example.demo.config.cache;

import com.example.demo.config.redis.RedisUtil;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 重写shiro自带的缓存管理器，换成重写的redis
 */
@Component
public class CustomCacheManager implements CacheManager {

//    private RedisTemplate<String, Object> redisTemplate;
//
//    public CustomCacheManager(RedisTemplate<String, Object> redisTemplate) {
//        this.redisTemplate = redisTemplate;
//    }

    @Autowired
    RedisUtil redisUtil;

    @Override
    public <K, V> Cache<K, V> getCache(String name) throws CacheException {
//        return new CustomCache<K, V>(redisTemplate);
        return new CustomCache<K, V>(redisUtil);
    }
}
