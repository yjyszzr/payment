package com.dl.shop.payment.pay.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dl.shop.payment.web.PaymentController;

/***
 * Http工具类
 */
public class HttpUtil {
	private final static Logger logger = LoggerFactory.getLogger(PaymentController.class);
	
	 public static RspHttpEntity sendMsg(String postData, String postUrl,boolean isJson) {
		 logger.info("postUrl={},postData={}",postUrl,postData);
		 RspHttpEntity rspEntity = new RspHttpEntity();
		 OutputStreamWriter out = null;
		 HttpURLConnection conn = null;
	        try {
	            //发送POST请求
	            URL url = new URL(postUrl);
	            conn = (HttpURLConnection) url.openConnection();
	            conn.setRequestMethod("POST");
	            if(isJson) {
	            	conn.setRequestProperty("Content-Type", "application/json");
	            }else {
	            	conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	            }
	            conn.setRequestProperty("Connection", "Keep-Alive");
	            conn.setUseCaches(false);
	            conn.setDoOutput(true);
	            if(postData != null && postData.length() > 0) {
	            	conn.setRequestProperty("Content-Length", "" + postData.length());
		            out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
		            out.write(postData);
		            out.flush();
	            }
	            int code = conn.getResponseCode();
	            //获取响应状态
	            if (code != HttpURLConnection.HTTP_OK) {
	                System.out.println("connect failed code:" + code);
	                rspEntity.isSucc = false;
	                rspEntity.msg = "code:" + code;
	                return rspEntity;
	            }
	            //获取响应内容体
	            String line, result = "";
	            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
	            while ((line = in.readLine()) != null) {
	                result += line + "\n";
	            }
	            in.close();
	            logger.info("HttpUtil.sendMsg response={}",result);
	            rspEntity.msg = result;
	            rspEntity.isSucc = true;
	            return rspEntity;
	        } catch (IOException e) {
	        	rspEntity.isSucc = false;
	        	rspEntity.msg = e.getMessage();
	            logger.error("",e);
	        }finally {
	        	if(out != null) {
	        		try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
	        	}
	        }
	        return rspEntity;
	    }
	 
}
