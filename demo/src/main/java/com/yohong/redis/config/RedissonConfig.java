package com.yohong.redis.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

/**
 * @author fanyufeng
 * @version 1.0
 * @description desc the function of class
 * @date 2024/6/7 17:00
 */
public class RedissonConfig {
	
	public static RedissonClient createClient() {
		Config config = new Config();
		config.useSingleServer()
				.setAddress("redis://127.0.0.1:6379")
				.setPassword("123456");
		return Redisson.create(config);
	}
	
}
