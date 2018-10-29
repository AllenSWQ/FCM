package com.push.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

public class DateUtil extends DateUtils {
	private static SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	/**
	 * 获取前一天时间 yyyy-MM-dd
	 * @return
	 */
	public static String getDateYMD(int num){
		Calendar calendar = Calendar.getInstance();
	    calendar.add(Calendar.DAY_OF_MONTH, num);   
		String time = sd.format(calendar.getTime());	
		return time;		
	}
	
	/**
	 * 获取前一天时间 yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String getDateYMDHMS(int num){
		Calendar calendar = Calendar.getInstance();
	    calendar.add(Calendar.DAY_OF_MONTH, num);   
		String time = sdf.format(calendar.getTime());	
		return time;		
	}

	public static int getHoursIntervalBetweenTwoDate(long startDate,
			long endDate, String brandName) {
		return (int) ((endDate - startDate) / (1000 * 60 * 60));
	}

	public static Date strToDate(String dateStr, String format)
			throws ParseException {
		SimpleDateFormat df = new SimpleDateFormat(format);
		Date date = df.parse(dateStr);// strDate 格式要和format对应
		return date;
	}

	public static void main(String[] args) throws ParseException {
	}
}
