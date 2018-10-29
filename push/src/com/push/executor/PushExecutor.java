package com.push.executor;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.push.model.Task;
import com.push.service.PushService;

public class PushExecutor {
	public static ThreadPoolExecutor threadPoolExecutor = null;
	
	private static PushExecutor pushExecutor = null;
	
	private PushExecutor(){
		if(threadPoolExecutor==null){
			threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
		}
	}
	
	/**
	 * 实例化
	 */
	public static PushExecutor getInstance(){
		if(pushExecutor==null){
			pushExecutor = new PushExecutor();
		}
		return pushExecutor;
	}
	
	/**
	 * 添加需要执行的线程
	 */
	public void executorTask(PushService pushService, List<Task> list) {
		for (int i = 0; i < list.size(); i++) {
			PushThread pushThread = new PushThread(pushService, list.get(i));
			threadPoolExecutor.execute(pushThread);
		}
	}
	
	/**
	 * 停止线程
	 */
	public void shutdownExecutor(ThreadPoolExecutor executor){
		executor.shutdown();
	}
}
