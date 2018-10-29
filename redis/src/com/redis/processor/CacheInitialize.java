package com.redis.processor;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.task.task;

public class CacheInitialize implements InitializingBean {
	
	@Autowired
	private task pushTask;

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
	}

}
