package com.yohong.redis.lock;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.concurrent.TimeUnit;

/**
 * @author fanyufeng
 * @version 1.0
 * @description desc the function of class
 * @date 2024/6/7 16:33
 */
public class RedissonLock {
	
	
	public static void main(String[] args) {
		// 1.构造redisson实现分布式锁必要的Config
		Config config = new Config();
		config.useSingleServer().setAddress("redis://127.0.0.1:5379").setPassword("123456").setDatabase(0);
		// 2.构造RedissonClient
		RedissonClient redissonClient = Redisson.create(config);
		
		String lockKey = "123";
		// 3.获取锁对象实例（无法保证是按线程的顺序获取到）
		RLock rLock = redissonClient.getLock(lockKey);
		try {
			/*
			 * 4.尝试获取锁
			 * waitTimeout 尝试获取锁的最大等待时间，超过这个值，则认为获取锁失败
			 * leaseTime   锁的持有时间,超过这个时间锁会自动失效（值应设置为大于业务处理的时间，确保在锁有效期内业务能处理完）
			 */
			int waitTimeout = 1000, leaseTime = 1000;
			
			boolean res = rLock.tryLock((long)waitTimeout, (long)leaseTime, TimeUnit.SECONDS);
			if (res) {
				//成功获得锁，在这里处理业务
			}
		} catch (Exception e) {
			throw new RuntimeException("aquire lock fail");
		}finally{
			//无论如何, 最后都要解锁
			rLock.unlock();
		}
	}

}
