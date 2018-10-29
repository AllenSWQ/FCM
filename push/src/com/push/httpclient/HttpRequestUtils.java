package com.push.httpclient;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class HttpRequestUtils {
    private static Log logger=LogFactory.getLog(HttpRequestUtils.class);
    
    /**
     * post请求
     * @param url         url地址
     * @param jsonParam     参数
     * @param noNeedResponse    不需要返回结果
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static String httpPost(String url,Map<String,String> map){
       
        DefaultHttpClient httpClient = new DefaultHttpClient();
        String str = "";
        try {
            url = URLDecoder.decode(url, "UTF-8");
            HttpPost method = new HttpPost(url);
            List<NameValuePair> list =new ArrayList<NameValuePair>();
            Iterator iterator = map.entrySet().iterator();  
            while(iterator.hasNext()){  
                Entry<String,String> elem = (Entry<String, String>) iterator.next();  
                list.add(new BasicNameValuePair(elem.getKey(),elem.getValue()));  
            }  
            if(list.size() > 0){  
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list,"utf-8");  
                method.setEntity(entity);  
            }  
            HttpResponse result = httpClient.execute(method);
            logger.info("Http StatusCode = " + result.getStatusLine().getStatusCode());
            if (result.getStatusLine().getStatusCode() == 200) {
                try {
                    str = EntityUtils.toString(result.getEntity(),"utf-8");
                    if(StringUtils.isEmpty(str)){
                    	logger.info("Return Message is null, the dataJsonVal = " + map.get("dataJson"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("post请求提交失败:" + url, e);
                }
            } else {
            	 logger.info("Http StatusCode is not 200, the dataJsonVal = " + map.get("dataJson"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("post请求提交失败:" + url, e);
        }
        return str;
    }
 
 
    /**
     * 发送get请求
     * @param url    路径
     * @return
     */
    public static String httpGet(String url){
	 String strResult="";
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            url = URLDecoder.decode(url, "UTF-8");
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                strResult = EntityUtils.toString(response.getEntity());
            } else {
                logger.error("get请求提交失败:" + url);
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("get请求提交失败:" + url, e);
        }
        return strResult;
    }
    
    
	public static void main(String[] args) {
	}
    
}
