package com.hairbyprogress.recyclerview.util;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;

/**
 * 存储工具类
 * 
 * @author YZQ
 * 
 */
class StorageUtil {
	/**
	 * 获取内存存储可用空间
	 * 
	 * @return long
	 */
	public static long getAvailableInternalMemorySize() {
		File path = Environment.getDataDirectory(); // 获取数据目录
		StatFs stat = new StatFs(path.getAbsolutePath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	/**
	 * 获取内存存储总空间
	 * 
	 * @return long
	 */
	public static long getTotalInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getAbsolutePath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	/**
	 * 判断外部存储是否可用
	 * 
	 * @return
	 */
	private static boolean isExternalStorageAvailable() {
		return Environment.MEDIA_MOUNTED
				.equals(Environment.getExternalStorageState());
	}

	/**
	 * 获取外部可用空间大小
	 * 
	 * @return
	 */
	public static long getAvailableExternalMemorySize() {
		if (isExternalStorageAvailable()) {
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getAbsolutePath());
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			return availableBlocks * blockSize;
		} else {
			return -1;
		}
	}

	/**
	 * 获取外部总共空间大小
	 * 
	 * @return
	 */
	public static long getTotalExternalMemorySize() {
		if (isExternalStorageAvailable()) {
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getAbsolutePath());
			long blockSize = stat.getBlockSize();
			long totalBlocks = stat.getBlockCount();
			return totalBlocks * blockSize;
		} else {
			return -1;
		}
	}
}
