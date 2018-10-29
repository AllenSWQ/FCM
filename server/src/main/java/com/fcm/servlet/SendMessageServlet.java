package com.fcm.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fcm.server.FCMServer;

public class SendMessageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Log logger = LogFactory.getLog(SendMessageServlet.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		String line = request.getParameter("line");
		String regIds = request.getParameter("regIds");
		String apiKey = request.getParameter("apiKey");
		String dataJson = request.getParameter("dataJson");
		logger.info("SendMessageServlet >>>> requset params is >>>> line >>>> "
				+ line + " >>>> regIds >>>> " + regIds + " >>>> apiKey >>>> "
				+ apiKey + " >>>> dataJson >>>> " + dataJson);
		
		PrintWriter out;
		
		// 验证参数非空
		if (StringUtils.isEmpty(line) || StringUtils.isEmpty(regIds)
				|| StringUtils.isEmpty(apiKey) || StringUtils.isEmpty(dataJson)) {
			logger.info("SendMessageServlet >>>> requset params exist null");
			JSONObject jb = new JSONObject();
			jb.put("status_code", 999);
			jb.put("message", "error params");
			try {
				out = response.getWriter();
				out.write(jb.toString() + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
		
			JSONObject json = new JSONObject();
	
			String[] registrationIds = regIds.split(",");
			int count = registrationIds.length;
			int batch = count / 1000;
	
			for (int jj = 0; jj < batch + 1; jj++) {
				JSONArray ids = new JSONArray();
				for (int ii = jj * 1000; ii < (jj + 1) * 1000; ii++) {
					if (ii >= count)
						break;
					ids.add(registrationIds[ii]);
				}
				
				json.put("registration_ids", ids);
				json.put("data", dataJson);
				logger.info(json.toString());
				byte[] bytes = json.toString().getBytes();
				String result = FCMServer.sendData(bytes, apiKey);
				try {
					out = response.getWriter();
					out.write(result + "\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				logger.info("SendMessageServlet >>>>  post data is >>>> line >>>> "
						+ line + " >>>> regIds >>>> " + regIds
						+ " >>>> apiKey >>>> " + apiKey + " >>>> dataJson >>>> "
						+ dataJson + " >>>> postdata >>>> " + json.toString()
						+ " >>>> result:" + result);
			}
		}

	}

}
