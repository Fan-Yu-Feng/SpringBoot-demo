package com.yohong.utils;

import java.io.File;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author fanyufeng
 * @date 2024/3/6 11:16
 * @description 文件处理工具类
 */
public class FileUtil {
	
	
	
	// 最大盘符文件
	public static File maxStorageDrive = null;
	
	
	public static void main(String[] args) {
		File[] roots = File.listRoots();
		File maxStorageDrive = findMaxStorageDrive();
		
		
		System.out.println("maxStorageDrive = " + maxStorageDrive);
	}
	
	
	/**
	 * 获取 windows 最大的盘符
	 *
	 * @return 最大盘符的文件
	 */
	public synchronized static File findMaxStorageDrive() {
		if (maxStorageDrive != null) {
			return maxStorageDrive;
		}
		File[] roots = File.listRoots();
		long maxStorage = Long.MIN_VALUE;
		
		for (File root : roots) {
			long storage = getUsableSpace(root);
			if (storage > maxStorage) {
				maxStorage = storage;
				maxStorageDrive = root;
			}
		}
		return maxStorageDrive;
	}
	
	private static long getUsableSpace(File file) {
		try {
			Path path = file.toPath();
			FileStore fileStore = Files.getFileStore(path);
			
			return fileStore.getUsableSpace();
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	
}
