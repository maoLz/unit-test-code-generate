package com.alizo.zTest.base;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ConcurrentUtils {
	public static int CORES = Runtime.getRuntime().availableProcessors();

	// 创建线程数固定为处理器核心数的线程池（类似于Executors.newFixedThreadPool(CORES)，但使用共享的长度为2000的有界队列）
	private static ThreadPoolExecutor EXESVR = new ThreadPoolExecutor(10, 10, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2000));

	// 创建单线程池
	private static ThreadPoolExecutor SINGLEWORK = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2000));

	public static <T> Future<T> submitTask(Callable<T> task) {
		return EXESVR.submit(task);
	}

	public static Future<?> submitTask(Runnable task) {
		return EXESVR.submit(task);
	}

	public static void awaitTermination() throws InterruptedException {
			awaitTermination(Long.MAX_VALUE,TimeUnit.MINUTES);
	}
	public static void awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
			EXESVR.awaitTermination(timeout,unit);
	}


	public static <T> Future<T> submitSingleThreadTask(Callable<T> task) {
		return SINGLEWORK.submit(task);
	}

	public static Future<?> submitSingleThreadTask(Runnable task) {
		return SINGLEWORK.submit(task);
	}
}
