package com.yohong.redis.lock;

import com.yohong.redis.config.RedissonConfig;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * @author fanyufeng
 * @version 1.0
 * @description desc the function of class
 * @date 2024/6/7 17:06
 */
public class ReentrantLockExample {
	
	
	private static final RedissonClient redissonClient = RedissonConfig.createClient();
	
	public void performTaskWithLock() {
		RLock lock = redissonClient.getLock("myLock");
		
		try {
			// 尝试获取锁，等待时间为10秒，锁的持有时间为1分钟
			if (lock.tryLock(10, 60, TimeUnit.SECONDS)) {
				try {
					// 执行需要加锁的代码
					System.out.println("Lock acquired, executing task...");
					// 模拟任务执行
					Thread.sleep(5000);
				} finally {
					lock.unlock();
					System.out.println("Lock released.");
				}
			} else {
				System.out.println("Unable to acquire lock, task skipped.");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		ReentrantLockExample example = new ReentrantLockExample();
		example.performTaskWithLock();
	}
}
