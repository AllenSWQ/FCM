package com.push.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 日志打印
 */
public class LogUtil {

	private static Log pushLog = LogFactory.getLog("pushLog");

	/**
	 * 结果日志
	 * 
	 * @param arg
	 */
	public static synchronized void pushLog(String arg) {
		pushLog.info(arg);
	}
	
}
