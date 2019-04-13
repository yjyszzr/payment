package com.dl.shop.payment.pay.lidpay.util;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.dl.shop.payment.configurer.WxpayConfig;
import com.dl.shop.payment.pay.rongbao.config.ReapalH5Config;
import com.dl.shop.payment.web.PaymentController;

import lombok.Data;
@Data
@Configuration
public class LidPayH5Utils {
	private final static Logger logger = LoggerFactory.getLogger(PaymentController.class);
	/**
	 * 服务端地址
	 */
	@Value("${lid.pay.url}")
	private String PATH;
	/**
	 * 订单支付回调URL地址,需要修改为自己的异步回调地址，公网可以访问的
	 */
	@Value("${lid.pay.notifyUrl}")
	private String NOTIFY_URL;
	/**
	 * 订单支付同步URL地址,需要修改为自己的同步回调地址，公网可以访问的,本同步回调地址只在微信H5支付中使用payUrl时起作用
	 */
	@Value("${lid.pay.returnUrl}")
	private String RETURN_URL;
	/**
	 * 商户号，正式上线需要修改为自己的商户号
	 */
	@Value("${lid.pay.merchant}")
	private String MERCHANT_NO;
	/**
	 * 商户密钥，正式上线需要修改为自己的商户密钥
	 */
	@Value("${lid.pay.secret}")
	private String SECRET;
	/**
	 * 支付生成URL
	 */
	@Value("${lid.pay.url.paymethod}")
	private String PAY_URL_METHOD;
	/**
	 * 订单查询URL
	 */
	@Value("${lid.pay.url.querymethod}")
	private String QUERY_URL_METHOD;
	/**
	 * 订单退款URL
	 */
	@Value("${lid.pay.url.refundmethod}")
	private String REFUND_URL_METHOD;
	/**
	 * 订单退款URL
	 */
	@Value("${lid.pay.version}")
	private String VERSION;

	/**
	 * 发送消息体到服务端
	 * 
	 * @param params
	 * @return
	 */
	public static String sendPostMessage(URL url, Map<String, String> params) {
		StringBuilder stringBuilder = new StringBuilder();
		if (params != null && !params.isEmpty()) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				try {
					stringBuilder.append(entry.getKey()).append("=")
							.append(URLEncoder.encode(entry.getValue(), "utf-8")).append("&");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			stringBuilder.deleteCharAt(stringBuilder.length() - 1);
			System.out.println("url=" + url.toString() + "?" + stringBuilder.toString());
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
	private static String changeInputStream(InputStream inputStream) {

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
	 * @param params 签名参数
	 * @return sign
	 */
	private String getSign(Map<String, String> params) {

		// 先将参数以其参数名的字典序升序进行排序
		Map<String, String> sortedParams = new TreeMap<String, String>(params);
		Set<Entry<String, String>> entrys = sortedParams.entrySet();

		// 遍历排序后的字典，将所有参数按"key=value"格式拼接在一起
		StringBuilder basestring = new StringBuilder();
		for (Entry<String, String> param : entrys) {
			basestring.append(param.getKey()).append("=").append(param.getValue());
		}
		basestring.append(SECRET);
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
		return sign.toString();
	}

	/**
	 * 验签，验证返回参数签名。异步回调时，将所有post参数放入Map集合中，调用该方法即可进行参数验证
	 * 
	 * @param params 返回参数
	 * @return
	 */
	public boolean checkParamSign(Map<String, String> params) {
		boolean result = false;
		try {
			if (params == null || params.size() == 0) {
				return result;
			}
			if (params.containsKey("sign")) {
				String sign = params.get("sign");
				params.remove("sign");
				String signRecieve = null;
				signRecieve = getSign(params);
				result = sign.equalsIgnoreCase(signRecieve);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 测试订单订单生成，当返回result中code=1时，代表订单生成成功，需要验签
	 * @throws MalformedURLException 
	 */
	@SuppressWarnings("unused")
	public Map<String, String> pay(Map<String, String> param) throws MalformedURLException {
		Map<String, String> params = new HashMap<>();
		params.put("merchantNo", MERCHANT_NO);// 商户号
		params.put("notifyUrl", NOTIFY_URL);// 回调
		params.put("returnUrl", RETURN_URL);// 回调
		params.put("payMethod", param.get("payMethod"));// 支付方式
		params.put("version", VERSION);// 版本固定值
		params.put("name", param.get("name"));
		params.put("orderNo", param.get("orderNo"));
		params.put("total", param.get("total"));
		params.put("timestamp", String.valueOf(System.currentTimeMillis()));
		params.put("sign", getSign(params));
		// ************订单生成，当返回result中code=1时，代表订单生成成功，需要验签************
		logger.info("华移支付:PAY_URL={}",PATH + PAY_URL_METHOD);
		String result = sendPostMessage(new URL(PATH + PAY_URL_METHOD), params);
		logger.info("华移支付请求结果:result={}",result);
		// 校验返回值
		if (result != null && !"".equals(result)) {
			@SuppressWarnings("unchecked")
			Map<String, Object> obj = JSONObject.parseObject(result, Map.class);
			int code = (int) obj.get("code");
			if (code == 1)// 成功
			{
				@SuppressWarnings("unchecked")
				Map<String, String> resultObj = (Map<String, String>) obj.get("result");
				// 验证返回参数签名
				if (checkParamSign(resultObj)) {
					// 获取payUrl
//					String payUrl = resultObj.get("payUrl");
					return resultObj;
				} else {
					return null;
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * 测试订单查询，当返回result中status=1时，代表支付成功，需要验签
	 * @throws MalformedURLException 
	 */
	@SuppressWarnings("unused")
	public Map<String, String> orderQuery(String orderNo) throws MalformedURLException {

		Map<String, String> params = new HashMap<>();
		params.put("merchantNo", MERCHANT_NO);
		params.put("orderNo", orderNo);
		params.put("timestamp", String.valueOf(System.currentTimeMillis()));
		params.put("sign", getSign(params));
		// ************订单查询，当返回result中status=1时，代表支付成功，需要验签************
		// status状态：0:等待支付；1：支付成功；2：支付失败；3：订单已撤销；4：订单已退款
		logger.info("华移支付:QUERY_URL={}",PATH + QUERY_URL_METHOD);
		String result = sendPostMessage(new URL(PATH + QUERY_URL_METHOD), params);
		logger.info("华移支付订单查询请求结果:result={}",result);
		// 校验返回值
		if (result != null && !"".equals(result)) {
			@SuppressWarnings("unchecked")
			Map<String, Object> obj = JSONObject.parseObject(result, Map.class);
			int code = (int) obj.get("code");
			if (code == 1)// 成功
			{
				@SuppressWarnings("unchecked")
				Map<String, String> resultObj = (Map<String, String>) obj.get("result");
				// 验证返回参数签名
				if (checkParamSign(resultObj)) {
					// 获取status
					String status = resultObj.get("status");
					if ("1".equals(status)) {
						return resultObj;
					}else {
						return null;
					}
				} else {
					return null;
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * 测试订单退款，全额退款，简单接口，无须验签
	 * @throws MalformedURLException 
	 */
	@SuppressWarnings("unused")
	public void orderRefund(Map<String, String> param) throws MalformedURLException {

		Map<String, String> params = new HashMap<>();
		params.put("merchantNo", MERCHANT_NO);
		params.put("orderNo", param.get("orderNo"));// 单号
		params.put("refundFee", param.get("total"));// 退款金额
		params.put("refundReson", param.get("refundReson"));// 退款原因
		params.put("timestamp", String.valueOf(System.currentTimeMillis()));
		params.put("sign", getSign(params));

		// ************订单退款，全额退款，简单接口，无须验签************
		logger.info("华移支付:REFUND_URL={}",PATH + REFUND_URL_METHOD);
		String result = sendPostMessage(new URL(PATH + REFUND_URL_METHOD), params);
		System.out.println(result);

		// 校验返回值
		if (result != null && !"".equals(result)) {
			@SuppressWarnings("unchecked")
			Map<String, Object> obj = JSONObject.parseObject(result, Map.class);
			int code = (int) obj.get("code");
			if (code == 1)// 成功
			{
				System.out.println("退款成功。");
			} else {
				String msg = (String) obj.get("msg");
				System.out.println("失败，原因：" + msg);
			}
		} else {
			System.out.println("服务器连接异常，请重试！");
		}
	}

//	public static void main(String[] args) {
//		System.out.println(PATH);
//		String orderNo = "20180818134613";
//		String total = "100";
//		String payMethod = "6015";
//		String name="绿茶";//商品名称
//		String refundReson="协商退款";
//		Map<String,String> param = new HashMap<>();
//		param.put("orderNo", orderNo);
//		param.put("total", total);
//		param.put("payMethod", payMethod);
//		param.put("name", name);
//		param.put("refundReson", refundReson);
//		// ************订单生成，当返回result中code=1时，代表订单生成成功，需要验签************
//		pay(param);
//
//		// ************订单查询，当返回result中status=1时，代表支付成功，需要验签************
//		orderQuery(param);
//
//		// ************订单退款，全额退款，简单接口，无须验签************
//		orderRefund(param);
//	}
}
