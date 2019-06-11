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
