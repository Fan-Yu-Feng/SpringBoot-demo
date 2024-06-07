package com.yohong.redis.lock;

import com.yohong.redis.config.RedissonConfig;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;

/**
 * @author fanyufeng
 * @version 1.0
 * @description desc the function of class
 * @date 2024/6/7 17:11
 */
public class ReadWriteLockExample {
	
	private static final RedissonClient redissonClient = RedissonConfig.createClient();
	
	public void performReadOperation() {
		RReadWriteLock rwLock = redissonClient.getReadWriteLock("myReadWriteLock");
		rwLock.readLock().lock();
		try {
			System.out.println("Read lock acquired, performing read operation...");
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			rwLock.readLock().unlock();
			System.out.println("Read lock released.");
		}
	}
	
	public void performWriteOperation() {
		RReadWriteLock rwLock = redissonClient.getReadWriteLock("myReadWriteLock");
		rwLock.writeLock().lock();
		try {
			System.out.println("Write lock acquired, performing write operation...");
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			rwLock.writeLock().unlock();
			System.out.println("Write lock released.");
		}
	}
	
	public static void main(String[] args) {
		ReadWriteLockExample example = new ReadWriteLockExample();
		example.performReadOperation();
		example.performWriteOperation();
	}

}


