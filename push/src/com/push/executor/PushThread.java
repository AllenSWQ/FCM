package com.push.executor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.push.constant.Constant;
import com.push.dbutil.DataSourceContextHolder;
import com.push.httpclient.HttpRequestUtils;
import com.push.mail.JavaMailSender;
import com.push.model.Device;
import com.push.model.Task;
import com.push.redis.RedisClient;
import com.push.service.PushService;
import com.push.util.DateUtil;
import com.push.util.LogUtil;

public class PushThread implements Runnable {
	private Log log = LogFactory.getLog(this.getClass());
	@SuppressWarnings("unused")
	private final PushService pushService;
	private Task task;
	private static final Map<String, String> sendIdApiKeys = new HashMap<>();

	public PushThread(PushService pushService, Task task) {
		this.pushService = pushService;
		this.task = task;
		if (sendIdApiKeys.isEmpty()) {
			initSendIdApiKeys();
		}
	}

	/**
	 * 初始化对应的sendId和apiKey
	 */
	private void initSendIdApiKeys() {
		sendIdApiKeys.put("sendid", "apikey");
	}

	@Override
	public void run() {
		pushStart();
	}

	/**
	 * 开始推送
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void pushStart() {
		log.info("task_id = " + task.getTid());
		try {
			List<Device> list = new ArrayList<>();
			Device sd = null;
			Map map = new HashMap<>();
			// 判断推送目标类型
			if (task.getTtarget() == 1) {// 1-批量
				list = getDeviceList();
			} else {// 2-单设备
				// 判断单设备类型
				if (task.getStype() == 1) {// 1-IMEI
					map.put("imei", task.getSimei());
					map.put("project", task.getSproject());
					DataSourceContextHolder.setDs("ds32");
					list = null;
				} else {// 0-FCMID
					list = new ArrayList<>();
					sd = new Device();
					sd.setFcmId(task.getSfcmId());
					list.add(sd);
				}
			}
			
			// 推送前 更新状态=进行中 预计目标=实际推送量
			beforePush(list.size());
			sendJson(list);

		} catch (Exception e) {
			e.printStackTrace();
			log.error("push exception >> task_id = " + task.getTid());
			// 出现异常 更新状态=失败 到达率=0% 并发送预警邮件
			exceptionHandle();
		}
	}

	/**
	 * 获取模糊匹配的key
	 * 
	 * @return
	 */
	private String getMatchKey() {
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
	private List<Device> getDeviceList() {
		String logstr = "task_id = " + task.getTid();
		String match_key = getMatchKey();
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
	 * 推送
	 */
	private void sendJson(List<Device> list) {
		List<String> result_list = new ArrayList<>();
		int pushSize = 1000;
		int arrayNum = 1;
		int residueNum = 0;
		try {
			if (CollectionUtils.isEmpty(list)) {// 设备list为空
			} else {
				Map<String, String> sentMap = new HashMap<String, String>();
				sentMap.put("line", "fota");
				sentMap.put("apiKey", sendIdApiKeys.get("1075259712158"));
				sentMap.put("dataJson", task.getMjson());
				
				residueNum = list.size() % pushSize;// 余数
				arrayNum = list.size() / pushSize;// 需遍历的次数
				if (residueNum != 0) {
					arrayNum++;
				}
				log.info("sendJson >> task_id = " + task.getTid() + " >> arrayNum = " + arrayNum + " >> mjson = " + task.getMjson());

				StringBuffer regIds = null;
				String result = "";
				for (int i = 1; i <= arrayNum; i++) {
					regIds = new StringBuffer();
					regIds.append(list.get((i - 1) * pushSize).getFcmId());
					
					if (i == arrayNum){
						for (int j = (i - 1) * pushSize + 1; j < list.size(); j++) {
							regIds.append("," + list.get(j).getFcmId());
						}
					} else {
						for (int j = (i - 1) * pushSize + 1; j < (i * pushSize); j++) {
							regIds.append("," + list.get(j).getFcmId());
						}
					}
					
					sentMap.put("regIds", regIds.toString());
					log.info("ThreadId = " + Thread.currentThread().getId() + " >> FCM Push >> url = " + Constant.FCM_SERVER_URL);
					result = HttpRequestUtils.httpPost(Constant.FCM_SERVER_URL, sentMap);
					log.info("ThreadId = " + Thread.currentThread().getId() + " >> FCM Push>> task_id = " + task.getTid() + ", arrayNum = " + i + ", regIds = " + regIds.toString() + ", result_json = " + result);
					log.info("ActiveCount = " + PushExecutor.threadPoolExecutor.getActiveCount() + ", PoolSize = " + PushExecutor.threadPoolExecutor.getPoolSize());
					result_list.add(result);
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("push exception >> task_id = " + task.getTid());
			// 出现异常 更新状态=失败 到达率=0% 并发送预警邮件
			exceptionHandle();
		}

		// 解析推送结果 1、更新后台统计表 2、详细数据落地
		analysisResult(list, result_list, arrayNum, residueNum, pushSize);
	}

	/**
	 * 分析结果：1-统计结果入后台库 2-详细数据落地log
	 * 
	 * @param device_list
	 * @param result_list
	 * @param arrayNum
	 * @param residueNum
	 * @param pushSize
	 */
	private void analysisResult(List<Device> device_list, List<String> result_list, int arrayNum, int residueNum, int pushSize) {
		log.info("analysisResult start");
		try {
			String push_date = DateUtil.getDateYMD(0);
			String push_time = DateUtil.getDateYMDHMS(0);
			if (CollectionUtils.isEmpty(device_list)){
				log.info("analysisResult >> device_list is null");
				// 推送后更新状态=已完成/失败 到达率=成功/总推送量
				afterPush(0);
			} else {
				int succ_num = 0;
				int fail_num = 0;
				JSONObject json = null;
				JSONObject message = null;
				JSONArray results = null;
				Device device = null;
				int i = 1;
				for (String result : result_list){
					log.info("analysisResult >> result_json = " + result);
					json = new JSONObject();
					try {
						json = JSONObject.parseObject(result);
						if (json.get("status_code").toString().equals("200")){
							log.info("analysisResult >> status_code = " + 200);
							message = new JSONObject();
							message = json.getJSONObject("message");
							succ_num = succ_num + Integer.parseInt(message.get("success").toString());
							fail_num = fail_num + Integer.parseInt( message.get("failure").toString());
							log.info("analysisResult >> succ_num = " + Integer.parseInt(message.get("success").toString()));
							log.info("analysisResult >> fail_num = " + Integer.parseInt(message.get("failure").toString()));
							
							// 遍历所有详细结果
							results = new JSONArray();
							results = JSONObject.parseArray(message.getString("results"));
							log.info("analysisResult >> results = " + results.toJSONString());
							
							JSONObject data = null;
							for (int j = 0; j < results.size(); j++) {
								device = device_list.get((i - 1) * pushSize + j);
								data = new JSONObject();
								data = results.getJSONObject(j);
								log.info("analysisResult >> data = " + data.toJSONString());
								if (StringUtils.isNotEmpty(data.getString("message_id"))){// 成功
									LogUtil.pushLog(task.getTid() + "|||" + device.getImei() + "|||"
											+ device.getFcmId() + "|||" + device.getSendId() + "|||success||||||" + push_date);
								} else {
									LogUtil.pushLog(task.getTid() + "|||" + device.getImei() + "|||"
											+ device.getFcmId() + "|||" + device.getSendId() + "|||fail|||" + data.getString("error") + "|||" + push_date);
								}
							}
							
						} else {
							log.info("analysisResult >> status_code = " + json.get("status_code"));
							if (i == arrayNum){
								fail_num = fail_num + residueNum;
							} else {
								fail_num = fail_num + pushSize;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						if (i == arrayNum){
							fail_num = fail_num + residueNum;
						} else {
							fail_num = fail_num + pushSize;
						}
					}
					i++;
				}
				
				log.info("analysisResult >> total_succ_num = " + succ_num);
				log.info("analysisResult >> total_fail_num = " + fail_num);
				// 推送后更新状态=已完成/失败 到达率=成功/总推送量
				afterPush(succ_num * 1.0 / (succ_num + fail_num));
				// 统计结果入后台库
				markPushStatistics(succ_num, fail_num, push_date, push_time);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("analysisResult exception >> task_id = " + task.getTid());
		}
		log.info("analysisResult end");
	}
	
	/**
	 * 出现异常 更新状态=失败 到达率=0% 并发送预警邮件
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void exceptionHandle() {
		try {
			Map map = new HashMap<>();
			map.put("tStatus", 4);
			map.put("tRate", 0);
			map.put("tid", task.getTid());
			DataSourceContextHolder.setDs("ds1");
			//pushService.updateFcmTask(map);
			
			// 发送邮件预警
			new JavaMailSender().sendMailContent(
					new String[] { "suweiquan@adups.com" }, null, "FCM PUSH",
					"push exception >> task_id = " + task.getTid());
			
		} catch (Exception e) {
			e.printStackTrace();
			log.error("exceptionHandle exception >> task_id = " + task.getTid());
		}
	}
	
	/**
	 * 推送前更新
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void beforePush(int tAmount) {
		try {
			Map map = new HashMap<>();
			map.put("tStatus", 3);
			map.put("tAmount", tAmount);
			map.put("tid", task.getTid());
			DataSourceContextHolder.setDs("ds1");
			//pushService.updateFcmTask(map);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("beforePush exception >> task_id = " + task.getTid());
		}
	}
	
	/**
	 * 推送后更新
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void afterPush(double tRate) {
		try {
			Map map = new HashMap<>();
			map.put("tStatus", (tRate * 100) < 50 ? 4 : 5);
			map.put("tRate", (tRate * 100));
			map.put("tid", task.getTid());
			DataSourceContextHolder.setDs("ds1");
			//pushService.updateFcmTask(map);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("afterPush exception >> task_id = " + task.getTid());
		}
	}
	
	/**
	 * 记录推送统计结果
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void markPushStatistics(int succ_num, int fail_num, String push_date, String push_time){
		try {
			Map map = new HashMap<>();
			map.put("tid", task.getTid());
			map.put("total_num", succ_num + fail_num);
			map.put("succ_num", succ_num);
			map.put("fail_num", fail_num);
			map.put("push_date", push_date);
			map.put("push_time", push_time);
			DataSourceContextHolder.setDs("ds1");
			//pushService.addFcmStatistics(map);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("markPushStatistics exception >> task_id = " + task.getTid());
		}
	}

}
