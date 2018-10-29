package com.push.dbutil;

public class DataSourceContextHolder {
	 
	    private static final ThreadLocal<String> contextHolder = new ThreadLocal<String>();  
	  
	    public static void setDs(String ds) {  
	        contextHolder.set(ds);  
	    }  
	  
	    public static String getDs() {  
	        return ((String) contextHolder.get());  
	    }  
	  
	    public static void clearDs() {  
	        contextHolder.remove();  
	    }  

}  
    

