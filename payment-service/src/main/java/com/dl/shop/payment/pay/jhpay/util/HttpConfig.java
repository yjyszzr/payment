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
	
	private PayConfigUtil pcutil;
	/**
	 * 获取用户UserId
	 */
	public static String getUserid(String code) {
		AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do",
				"2019061165500931",
				"MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDokHNqfrAPfa+7\r\n" + 
				"hSKgBnP/iPyU48ZzlE72MSAexXNwL9TzRQTQ7LoKJRmD6OFbiW8OwABiJczjrjDI\r\n" + 
				"8eosreelgtckXcI/NXkLae7FE4asY13D/3DyyXze7ApUuS3FHAXcj/mdrRWO3q+W\r\n" + 
				"iMNLcXYKf4DhwwUGS2IzK4TQJA2sQQi0QjLplXNGcCLhHQFqBUiWUOZ11UEArkpf\r\n" + 
				"WAFAeqOqxs21Xv9X+IpOINIFmJZt1wNe4pdd2MGg95boHnLiT5YcI+X45cVYSKvb\r\n" + 
				"D1FbC49kYMq+V1Ydw3OES+XeEiGvZDK6IUjkMHT2ea+l+d8Am5uAPbWDQI3jjvlh\r\n" + 
				"lgTyeRcXAgMBAAECggEAd3pGoS6Gut6iWp8yQ64tB9nTkZZXTOejjV19l/FutfMM\r\n" + 
				"3xHVQJRtm2ql6hvJMyKvGI/RYpry4QGLdKC74spRGLnYV4mHkrug/RkmHr9CT+wY\r\n" + 
				"runbmA+lhE0VnaMo/XvBEygwYC4cxjJnWNnYIzkeIJSSnOl4+lveDlXMPLZZA+XG\r\n" + 
				"ogYLPquHzPxDn0ICP0Dn3UaPF+rdl+WdJUJr9+/2FfQo3akDPys5pNsFBEXxKVPl\r\n" + 
				"ZJoqBAEa1JuWM1tdckupIn9LQKedj+x6ly0lhJcanT+YmlXnhhdqQa0BGquXScDu\r\n" + 
				"V/iKYwkJl+M3hv6q7ZS52e3itulxJDULQs8B1JoccQKBgQD0mGiXY+QjVAhMiLfy\r\n" + 
				"KQNTurgU8jbKxbb410n2VIkuUv0o4Uuds0LRa+OqPBv6G5QkUAWpfbHf30IRiW6a\r\n" + 
				"7ogYCaEKxg+b5SSxSwKqRp25xThfxOeJ7R03CCeqjXJNkhOWU6IzLpVPMmG6Vbgn\r\n" + 
				"e3cMnCSb3pREIDRZ3dyYPO0yKQKBgQDzaG8sL/VAxDq+iuZKku0F+MSiWqES0ymV\r\n" + 
				"nwVAWSbfUAfB91Vw1Wvk7/sZBs0Y5ss5zxDi6QBZUqZv7bnZ5r0Sjv3GduNnyzfp\r\n" + 
				"Or+d8zhZsM+7iIOmuGi8r4veQFr0/TgYw0wV+o7wcZUjVeG7UeX2ooctgzd55zH2\r\n" + 
				"x8AMy3mnPwKBgQDhLFHVVSuYbmr5cj/NWn5qnZGMDvPsNppceW3orShhEhtngAkp\r\n" + 
				"0/ambul3NcEXvj3iNB0STNns3E6pcFj3nrKBVpQAJBgIj6n44bJBaaMYe3yLhe0W\r\n" + 
				"J8jmecZyl6brzJflo3bGIZNpBlu7u+A90MbnP/Pf3sel8/Pd64aCTEydCQKBgQDQ\r\n" + 
				"9cjrAEjlzxA3X/sP7k55H/V/A5rgFFPQ1PGnKmIKuCPQysqY0T+NDNBdzc7pH8k7\r\n" + 
				"2Z2/jxPzmtazpDw26rVKZ2NJq+rRwk4/dWXm7VRk+zt63VlYGVwhD/tdU5ZCV9h+\r\n" + 
				"ubpp6+4mUPwdl67wJwDq2OB/m/RWPLpSB23CDjRj9QKBgQC/l2bbNYgFRfyv9hQ+\r\n" + 
				"Tq3rZBv7qBFdx/3WXY1WNDYSZCcoPA2+Bk3o4eodCX/D2QRhL8C9lc5ASQwWVjQR\r\n" + 
				"Oy5H7EaV71Z5re6rWV94idBpncLVOSQCm73CO/am5n89jkDKbI4h6BcMsLLD6XhJ\r\n" + 
				"W0Y7vFedTfKaUdR34DzWKTFcVw==",
				"json",
				"GBK",
				"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhz/sY09mWmmx8fA48MArLgO6fMQNWriqSS6fUPKP/ysWoa0MgJGdusRGOxldGYQlV2bPGiE74wcrV1b0VH6YdjETIkfTD5UwN/v+2G3gAfQffsy3EZ5U5oCNR7n5fBdIvBYqQ4js4bB5BERCpbhpqqqfw8fNcolWL5dPPlX9rVBpqKYBjp18e66v03hVLp7q9NIpGNZSJMgkMp9pIgKsyH1W928k/fn6RTP1VHVzeHl9/lJwturo66KyN98iCGsLSpMlZa6vRFescLHjrz/Nf29TI0VRmIlMrNsBG4Ic2AYXMJ5IjT7apb9XGQALgEEK41z9LKvcq7f9IyNb4us3cQIDAQAB",
				"RSA2");
		AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
		request.setGrantType("authorization_code");
		request.setCode(code);
		AlipaySystemOauthTokenResponse response;
		try {
			response = alipayClient.execute(request);
			if(response.isSuccess()){
				System.out.println("调用成功:"+response.getUserId());
				return response.getUserId();
				} else {
				System.out.println("调用失败");
				return null;
				}
		} catch (AlipayApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	@SuppressWarnings("finally")
	public String doPostMessage(String reqUrl,SortedMap<String, String> map){
		Map<String, String> params = SignUtils.paraFilter(map);
		StringBuilder buf = new StringBuilder((params.size() + 1) * 10);
		SignUtils.buildPayParams(buf, params, false);
		String preStr = buf.toString();
		String sign = MD5.sign(preStr, "&key=" + pcutil.getKEY(), "utf-8");
		map.put("sign", sign);

		reqUrl = pcutil.getPATH();
		System.out.println("reqUrl：" + reqUrl);
		CloseableHttpResponse response = null;
		CloseableHttpClient client = null;
		String res = null;
		try {
			HttpPost httpPost = new HttpPost(reqUrl);
			StringEntity entityParams = new StringEntity(XmlUtils.parseXML(map), "utf-8");
			httpPost.setEntity(entityParams);
			client = HttpClients.createDefault();
			response = client.execute(httpPost);
			if (response != null && response.getEntity() != null) {
				Map<String, String> resultMap = XmlUtils.toMap(EntityUtils.toByteArray(response.getEntity()), "utf-8");
				res = XmlUtils.toXml(resultMap);
				System.out.println("请求结果：" + res);

				if (resultMap.containsKey("sign")) {
					if (!SignUtils.checkParam(resultMap, pcutil.getKEY())) {
						res = "验证签名不通过";
					} else {
						if ("0".equals(resultMap.get("status")) && "0".equals(resultMap.get("result_code"))) {
							Map<String, String> orderResult = new HashMap<String, String>(); // 用来存储订单的交易状态(key:订单号，value:状态(0:未支付，1：已支付)) ---- 这里可以根据需要存储在数据库中
							orderResult.put(map.get("out_trade_no"), "0");// 初始状态

							String pay_info = resultMap.get("pay_info");
							Map payInfo = JsonUtil.jsonToMap(pay_info);
							String tradeNO = (String) payInfo.get("tradeNO");
							String code_img_url = resultMap.get("code_img_url");
						} else {
							
						}
					}
				}
			} else {
				res = "操作失败";
			}
		} catch (Exception e) {
			e.printStackTrace();
			res = "系统异常";
		} finally {
			System.out.println(res);
			try {
				if (response != null) {
					response.close();
				}
				if (client != null) {
					client.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return res;
		}
	}
}
