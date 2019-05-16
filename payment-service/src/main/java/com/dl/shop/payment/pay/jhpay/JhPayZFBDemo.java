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
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONObject;
import com.dl.base.util.DateUtil;
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
	private static String PATH = "http://47.98.199.152/api";
	/**
	 * 订单支付URL
	 */
	private String PAY_URL = "/gateway.php";
	/**
	 * 订单支付回调URL地址,需要修改为自己的异步回调地址，公网可以访问的
	 */
	private String NOTIFY_URL = "https://www.baidu.com";
	/**
	 * 商户号，正式上线需要修改为自己的商户号
	 */
	private String MERCHANT_NO = "153";
	/**
	 * 商户名称，正式上线需要修改为自己的商户号
	 */
	private String MERCHANT_NAME = "pankou28";
	/**
	 * 商户密钥，正式上线需要修改为自己的商户密钥
	 */
	private String SECRET = "e9b68d9976a7be489f88b211935aec1f";
	/**
	 * 商户号，(自定义商户号)
	 */
	private String ADDUID = "7927";

	/**
	 * 发送消息体到服务端
	 * 
	 * @param params
	 * @return
	 */
	public String sendPostMessage(URL url, Map<String, String> params) {
		StringBuilder stringBuilder = new StringBuilder();
		if (params != null && !params.isEmpty()) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				try {
					stringBuilder.append(entry.getKey()).append("=")
							.append(URLEncoder.encode(entry.getValue()==null?"":entry.getValue().toString(), "utf-8")).append("&");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			stringBuilder.deleteCharAt(stringBuilder.length() - 1);
			System.out.println("支付下单URL=" + url.toString() + "?" + stringBuilder.toString());
			try {
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setConnectTimeout(30000);
				urlConnection.setRequestMethod("POST"); // 以post请求方式提交
				urlConnection.setDoInput(true); // 读取数据
				urlConnection.setDoOutput(true); // 向服务器写数据
				// 获取上传信息的大小和长度
				byte[] myData = stringBuilder.toString().getBytes();
				// 设置请求体的类型是文本类型,表示当前提交的是文本数据
				urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				urlConnection.setRequestProperty("Content-Length", String.valueOf(myData.length));
				// 获得输出流，向服务器输出内容
				OutputStream outputStream = urlConnection.getOutputStream();
				// 写入数据
				outputStream.write(myData, 0, myData.length);
				outputStream.close();
				// 获得服务器响应结果和状态码
				int responseCode = urlConnection.getResponseCode();
				if (responseCode == 200) {
					// 取回响应的结果
					return changeInputStream(urlConnection.getInputStream());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return "";
	}

	/**
	 * 将一个输入流转换成指定编码的字符串
	 * 
	 * @param inputStream
	 * @param encode
	 * @return
	 */
	private String changeInputStream(InputStream inputStream) {

		// 内存流
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte[] data = new byte[1024];
		int len = 0;
		String result = null;
		if (inputStream != null) {
			try {
				while ((len = inputStream.read(data)) != -1) {
					byteArrayOutputStream.write(data, 0, len);
				}
				result = new String(byteArrayOutputStream.toByteArray(), "utf-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 获取签名
	 * 
	 * @param params
	 *            签名参数
	 * @return sign
	 */
	private String getSign(StringBuilder basestring) {
		basestring.append(SECRET);
		System.out.println(basestring.toString());
//		back_url=https://www.baidu.com&channel_id=9&exter_invoke_ip=192.168.31.13&notify_url=https://www.baidu.com&partner_id=11880031&partner_order=2018050211256811330028&pay_method=&payextra_param=&sign_type=MD5&total_fee=10&user_id=2&key=e1f88b0d13031f99acd6dbce553ded1f
		// 使用MD5对待签名串求签
		byte[] bytes = null;
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("MD5");
			bytes = md5.digest(basestring.toString().getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// 将MD5输出的二进制结果转换为小写的十六进制
		StringBuilder sign = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(bytes[i] & 0xFF);
			if (hex.length() == 1) {
				sign.append("0");
			}
			sign.append(hex);
		}
		return sign.toString().toUpperCase();
	}

	/**
	 * 测试订单订单生成，当返回result中code=1时，代表订单生成成功，需要验签
	 * @throws MalformedURLException 
	 */
	public void pay(Map<String, String> param) throws MalformedURLException {
		Map<String, String> params = new HashMap<>();
		params.put("out_trade_no", param.get("out_trade_no"));// 商户订单
		params.put("price", param.get("price"));// 订单金额
		params.put("istype", "1");// 支付方式(1: 支付宝扫码)
		params.put("adduid", ADDUID);// 商户id(你们定)
		params.put("adduser", MERCHANT_NAME);//商户网站的用户名(pankou28，不可修改)
		params.put("mct_id", MERCHANT_NO);//商户id(153,不可修改)
		StringBuilder basestring = new StringBuilder();
		basestring.append(params.get("out_trade_no")).append(params.get("price"))
				.append(params.get("istype")).append(ADDUID).append(MERCHANT_NAME).append(MERCHANT_NO);
		params.put("sign", getSign(basestring).toLowerCase());//签名 小写
		params.put("goodsname", param.get("goodsname"));//商品名(你们定, 不可用中文)
		params.put("adddata", param.get("goodsname"));//附加信息(你们定)
		params.put("notify_url", NOTIFY_URL);//成功支付后的异步通知地址
		params.put("return_url", NOTIFY_URL);//成功支付后的异步通知地址
		String result = sendPostMessage(new URL(PATH+PAY_URL), params);
		System.out.println(result);
		System.out.println(JSONUtils.toJSONString(params));
		
//		// 校验返回值
//		if (result != null && !"".equals(result)) {
//			if (!result.contains("code")) {
//				
////				Document doc = Jsoup.parse(result);
////				String url = doc.select("iframe").first().attr("src");
////				System.out.println(url);
//				return;
//			}
//			return;
////			}
//		} else {
//			System.out.println("服务器连接异常，请重试！");
//		}
	}


	public static void main(String[] args) throws MalformedURLException {
		JhPayZFBDemo apay = new JhPayZFBDemo();
		Map<String,String> param = new HashMap<>();
		param.put("out_trade_no", "CS12347129");// 商户订单
		param.put("price", "-1");// 订单金额 单位元
		param.put("goodsname", "测试");//商品名(你们定, 不可用中文)
		// ************订单生成，当返回result中code=1时，代表订单生成成功，需要验签************
		apay.pay(param);
	}

}
