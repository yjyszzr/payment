package com.dl.shop.payment.pay.jhpay;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONObject;
import com.dl.base.util.DateUtil;
import com.dl.shop.payment.pay.jhpay.util.JsonUtil;
import com.dl.shop.payment.pay.jhpay.util.MD5;
import com.dl.shop.payment.pay.jhpay.util.SignUtils;
import com.dl.shop.payment.pay.jhpay.util.XmlUtils;
import com.dl.shop.payment.utils.DateUtilPay;

/**
 * 华移支付工具类 。 由于需要解析返回的json数据，本工具类需要引入fastjson.jar第三方包
 * 
 * @author
 *
 */
public class JhPayZFBDemo {
	
	/**
	 * 服务端地址 
	 */
	private static String PATH = "https://pay.swiftpass.cn/pay/gateway";
	/**
	 * 商户号，正式上线需要修改为自己的商户号
	 */
	private String MERCHANT_NO = "288540005892";
	/**
	 * 商户密钥，正式上线需要修改为自己的商户密钥
	 */
	private String KEY = "e8b0bd096de520312c2a3c6ef2f36983";

	@SuppressWarnings("finally")
	public String doPostMessage(SortedMap<String, String> map){
		
		map.put("service", "pay.alipay.jspay");
		map.put("mch_id", MERCHANT_NO);
		map.put("mch_create_ip", "127.0.0.1");
		map.put("nonce_str", String.valueOf(new Date().getTime()));
		
		Map<String, String> params = SignUtils.paraFilter(map);
		StringBuilder buf = new StringBuilder((params.size() + 1) * 10);
		SignUtils.buildPayParams(buf, params, false);
		String preStr = buf.toString();
		String sign = MD5.sign(preStr, "&key=" + KEY, "utf-8");
		map.put("sign", sign);

		String reqUrl = PATH;
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
				System.out.println("Map转json:"+JSONUtils.toJSONString(resultMap));
				System.out.println("请求结果：" + res);

				if (resultMap.containsKey("sign")) {
					if (!SignUtils.checkParam(resultMap, KEY)) {
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
//			System.out.println(res);
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


	public static void main(String[] args) throws MalformedURLException {
		JhPayZFBDemo apay = new JhPayZFBDemo();
		SortedMap<String,String> param = new TreeMap<>();
//		param.put("service", "pay.alipay.jspay");
//		param.put("mch_id", apay.MERCHANT_NO);
//		param.put("out_trade_no", String.valueOf(new Date().getTime()));
//		param.put("body", "测试支付");
//		param.put("total_fee", "100");
//		param.put("mch_create_ip", "127.0.0.1");
//		param.put("notify_url", "http://www.baidu.com");
//		param.put("nonce_str", String.valueOf(new Date().getTime()));
//		param.put("buyer_id", "2088702691566268");
		
		param.put("out_trade_no", String.valueOf(new Date().getTime()));
		param.put("body", "测试支付");
		param.put("total_fee", "100");
		param.put("notify_url", "http://www.baidu.com");
		param.put("buyer_id", "2088702691566268");
		// ************订单生成，当返回result中code=1时，代表订单生成成功，需要验签************
		apay.doPostMessage(param);
	}

}
