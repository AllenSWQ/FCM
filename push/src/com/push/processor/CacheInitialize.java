package com.push.processor;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.push.task.pushTask;

/**
 * 启动初始化
 * @author suweiquan
 * @date 2018-09-12
 *
 */
public class CacheInitialize implements InitializingBean {
	
	@Autowired
	private pushTask pushTask;

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
	}

}
