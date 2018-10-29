package com.push.util;


/**
 * 工具类
 * @author suweiquan
 * @date 2018-09-13
 *
 */
public class Util {

	/**
	 * int转换
	 * 
	 * @param i
	 * @param c
	 * @return
	 */
	public static int paraInt(String i, int c) {
		try {
			if (i != null && !i.equals("")) {
				return Integer.parseInt(i);
			}
			return c;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return c;
		}
	}

	/**
	 * 字符串转换
	 * 
	 * @param object
	 * @param todata
	 * @return
	 */
	public static String isNull(String object, String todata) {
		if (object == null || object.equals("")) {
			return todata;
		} else {
			return object;
		}
	}
}
