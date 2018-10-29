package com.push.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Task implements Serializable {
	private String tid;// 任务ID
	private Integer ttarget;// 目标类型
	private Integer bproject;// 批量-项目
	private String bversion;// 批量-版本
	private Integer bimei;// 批量-特定IMEI
	private String bapk;// 批量-apk版本
	private Integer stype;// 单个-类型
	private String sfcmId;// 单个-FCMID
	private Integer sproject;// 单个-项目
	private String simei;// 单个-IMEI
	private String mjson;// 消息串

	public String getTid() {
		return tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

	public Integer getTtarget() {
		return ttarget;
	}

	public void setTtarget(Integer ttarget) {
		this.ttarget = ttarget;
	}

	public Integer getBproject() {
		return bproject;
	}

	public void setBproject(Integer bproject) {
		this.bproject = bproject;
	}

	public String getBversion() {
		return bversion;
	}

	public void setBversion(String bversion) {
		this.bversion = bversion;
	}

	public Integer getBimei() {
		return bimei;
	}

	public void setBimei(Integer bimei) {
		this.bimei = bimei;
	}

	public String getBapk() {
		return bapk;
	}

	public void setBapk(String bapk) {
		this.bapk = bapk;
	}

	public Integer getStype() {
		return stype;
	}

	public void setStype(Integer stype) {
		this.stype = stype;
	}

	public String getSfcmId() {
		return sfcmId;
	}

	public void setSfcmId(String sfcmId) {
		this.sfcmId = sfcmId;
	}

	public Integer getSproject() {
		return sproject;
	}

	public void setSproject(Integer sproject) {
		this.sproject = sproject;
	}

	public String getSimei() {
		return simei;
	}

	public void setSimei(String simei) {
		this.simei = simei;
	}

	public String getMjson() {
		return mjson;
	}

	public void setMjson(String mjson) {
		this.mjson = mjson;
	}

}
