package com.push.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.push.dao.PushDao;
import com.push.service.PushService;

@Service
public class PushServiceImpl implements PushService {

    @Resource
    private PushDao pushDao;

	
    
}
