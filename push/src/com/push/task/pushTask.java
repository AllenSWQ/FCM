package com.push.task;

import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.TypeReference;
import com.push.dbutil.DataSourceContextHolder;
import com.push.executor.PushExecutor;
import com.push.model.Device;
import com.push.model.Task;
import com.push.redis.RedisClient;
import com.push.service.PushService;
import com.push.util.LogUtil;

@Controller
@RequestMapping(value = "/pushTask")
public class pushTask {

	private Log log = LogFactory.getLog(this.getClass());
	@Autowired
	private PushService pushService;
	
	@Autowired
    private HttpServletRequest request;

	/**
	 * FCM PUSH
	 */
	public void work() {
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>[FCM PUSH START]<<<<<<<<<<<<<<<<<<<<<<<");
		PushExecutor.getInstance();
		push(0, null);
		while (PushExecutor.threadPoolExecutor.getActiveCount() != 0) {
			try {
				Thread.sleep(1000 * 30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>[FCM PUSH END]<<<<<<<<<<<<<<<<<<<<<<<");
	}

	/**
	 * 查询需要推送的任务
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping("/push.do")
	public @ResponseBody
	String push(Integer flag, String tid) {
		try {
			Map map = new HashMap<>();
			if (flag == 1){
				map.put("flag", flag);
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			map.put("tid", tid);
			DataSourceContextHolder.setDs("ds1");
			List<Task> list = null;
			log.info("push >> flag = " + flag + ", task_id = " + tid + ", list_size = " + list.size());
			
			if (CollectionUtils.isNotEmpty(list)) {
				PushExecutor.getInstance().executorTask(pushService, list);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "fail";
		}
		return "ok";
	}
	
	/**
	 * 查询任务的预计目标
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping("/getAmount.do")
	public @ResponseBody
	Integer getAmount(String tid) {
		String logstr = "getAmount >> task_id = " + tid;
		int amount = 0;
		try {
			Map map = new HashMap<>();
			map.put("flag", 1);
			map.put("tid", tid);
			DataSourceContextHolder.setDs("ds1");
			List<Task> list = null;
			log.info(logstr);
			
			if (CollectionUtils.isNotEmpty(list)) {
				log.info(logstr + " >> has task");
				amount = getAmountByTask(list.get(0));
			} else {
				log.info(logstr + " >> no task");
			}
		} catch (Exception e) {
			e.printStackTrace();
			amount = 0;
			log.info(logstr + " >> amout = " + amount);
		}
		log.info(logstr + " >> amout = " + amount);
		return amount;
	}
	
	/**
	 * 查询任务的预计目标
	 * @param flag 1:立刻推送 0:定时推送
	 */
	@RequestMapping("/getTarget.do")
	public @ResponseBody
	Integer getTarget() {
		String logstr = "";
		int amount = 0;
		try {
			//read object from stream 反序列化将stream生成Object对象
			ObjectInputStream ois = new ObjectInputStream(request.getInputStream());
			//HttpServletRequest req对象
			Object value = ois.readObject();
			Task task = (Task) value;
			
			logstr = "getTarget >> task_id = " + task.getTid();
			amount = getAmountByTask(task);
		} catch (Exception e) {
			e.printStackTrace();
			amount = 0;
			log.info(logstr + " >> amout = " + amount);
		}
		log.info(logstr + " >> amout = " + amount);
		return amount;
	}
	
	/**
	 * 获取任务的预计目标
	 * @param task
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Integer getAmountByTask(Task task){
		int amount = 0;
		try {
			List<Device> list = new ArrayList<>();
			// 判断推送目标类型
			if (task.getTtarget() == 1) {// 1-批量
				list = getDeviceList(task);
				amount = list.size();
			} else {// 2-单设备
				// 判断单设备类型
				if (task.getStype() == 1) {// 1-IMEI
					log.info("单台设备-IMEI start");
					DataSourceContextHolder.setDs("ds32");
					Map map = new HashMap<>();
					map.put("imei", task.getSimei());
					map.put("project", task.getSproject());
					list = null;
					log.info("单台设备-IMEI list = " + list);
					amount = list.size();
					log.info("单台设备-IMEI end");
				} else {// 0-FCMID
					amount = 1;
				}
			}
		} catch (Exception e){
			e.printStackTrace();
			return 0;
		}
		return amount;
	}
	
	/**
	 * 获取模糊匹配的key
	 * 
	 * @return
	 */
	public String getMatchKey(Task task) {
		StringBuffer key = new StringBuffer();
		key.append("FI#");
		if (task.getBproject() == 0) {
			key.append("*#");
		} else {
			key.append(task.getBproject() + "#");
		}
		if (StringUtils.isEmpty(task.getBversion())
				|| "0".equals(task.getBversion())) {
			key.append("*#");
		} else {
			key.append(task.getBversion() + "#");
		}
		if (StringUtils.isEmpty(task.getBapk()) || "0".equals(task.getBapk())) {
			key.append("*");
		} else {
			key.append(task.getBapk() + "*");
		}
		return key.toString();
	}

	/**
	 * 获取所有设备数据
	 * 
	 * @return
	 */
	@SuppressWarnings("static-access")
	public List<Device> getDeviceList(Task task) {
		String logstr = "getAmount >> task_id = " + task.getTid();
		String match_key = getMatchKey(task);
		log.info(logstr + " >> match_key = " + match_key);
		Set<String> key_list = RedisClient.getInstance().getKeys(match_key);

		List<Device> list = new ArrayList<>();
		for (String key : key_list) {
			log.info(logstr + " >> key = " + key);
			list.addAll(RedisClient.getInstance().getList(key,
					new TypeReference<List<Device>>() {
					}));
		}

		return list;
	}
	
	/**
	 * 保证日志按时生成
	 */
	public void doPushLog() {
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>[doPushLog START]<<<<<<<<<<<<<<<<<<<<<<<");
		LogUtil.pushLog("||||||||||||||||||");
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>[doPushLog END]<<<<<<<<<<<<<<<<<<<<<<<");
	}

}
