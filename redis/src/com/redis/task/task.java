package com.redis.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.redis.service.RedisService;

@Controller
@RequestMapping(value = "/task")
public class task {
	@Autowired
	private RedisService redisService;

	public void main(){
		
	}

}
