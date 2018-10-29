package com.fcm.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FCMServer {
	private static final Log logger = LogFactory.getLog(FCMServer.class);
	private static String fcm_url = "https://fcm.googleapis.com/fcm/send";
	//private static String fcm_url = "https://gcm-http.googleapis.com/gcm/send";
	
	public static String sendData(byte[] bytes, String apiKey) {
		JSONObject jb = new JSONObject();
		StringBuffer result = new StringBuffer();
		try {
			URL url = new URL(fcm_url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Authorization", "key=" + apiKey);
			OutputStream out = conn.getOutputStream();

			out.write(bytes);
			out.flush();
			out.close();
			
			int state_code = conn.getResponseCode();
			logger.info("Http StatusCode = " + state_code);
			if (state_code == HttpURLConnection.HTTP_OK) {
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(
							conn.getInputStream(), "UTF-8"));
					String line = null;
					while ((line = reader.readLine()) != null) {
						result.append(line);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			jb.put("status_code", state_code);
			jb.put("message", result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jb.toString();
	}

}