package com.mmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by Alex Cheng
 * 4/22/2020 1:30 PM
 */
public class TokenCache {

    private static Logger logger = LoggerFactory.getLogger(TokenCache.class); //日志打开
    //private static Logger logger = LoggerFactory.getLogger(getClass()); //getClass是动态的，静态不能引用动态

    public static final String TOKEN_PREFIX = "token_";
    // guava初始化缓存,LRU算法
    private static LoadingCache<String, String> localCache = CacheBuilder.newBuilder()
            .initialCapacity(1000).maximumSize(10000).expireAfterAccess(12, TimeUnit.HOURS) //12小时有效期
            .build(new CacheLoader<String, String>() { // 匿名实现类
                // 数据加载实现，当调用get取值时，如果key没有对应的值，就调用这个方法加载
                @Override
                public String load(String s) throws Exception {
                    return "null"; //返回一个字符串的null
                }
            });

    public static void setKey(String key,String value){
        localCache.put(key,value);
    }

    public static String getKey(String key){
        String value = null;
        try{
            value = localCache.get(key);
            if("null".equals(value)){
                return null;
            }
            return value;
        }catch (Exception e){
            logger.error("localCache get error", e);
        }
        return null;
    }


}
