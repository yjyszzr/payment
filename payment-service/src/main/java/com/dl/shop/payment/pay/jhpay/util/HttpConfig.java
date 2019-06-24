package com.dl.shop.payment.pay.jhpay.util;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;


/**
 * <一句话功能简述> <功能详细描述>测试支付
 * 
 * @author Administrator
 * @version [版本号, 2014-8-28]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class HttpConfig {
	private static final long serialVersionUID = 1L;
	/**
	 * 获取用户UserId
	 */
	public static String getUserid(String appid,String code,String private_key,String public_key) {
		AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do",
				appid,private_key,"json","GBK",public_key,"RSA2");
		AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
		request.setGrantType("authorization_code");
		request.setCode(code);
		AlipaySystemOauthTokenResponse response;
		try {
			response = alipayClient.execute(request);
			if(response.isSuccess()){
				return response.getUserId();
				} else {
				return null;
				}
		} catch (AlipayApiException e) {
			e.printStackTrace();
			return null;
		}
	}
}
