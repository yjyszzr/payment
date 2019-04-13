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
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.alibaba.fastjson.JSONObject;

/**
 * 华移支付工具类 。 由于需要解析返回的json数据，本工具类需要引入fastjson.jar第三方包
 * 
 * @author
 *
 */
public class LidPayDemo {

	/**
	 * 服务端地址
	 */
	private static String PATH = "http://118.24.55.13/pay";
	/**
	 * 支付生成URL
	 */
	private static URL PAY_URL;
	/**
	 * 订单查询URL
	 */
	private static URL QUERY_URL;
	/**
	 * 订单退款URL
	 */
	private static URL REFUND_URL;
	/**
	 * 订单支付回调URL地址,需要修改为自己的异步回调地址，公网可以访问的
	 */
	private static String NOTIFY_URL = "https://www.baidu.com";
	/**
	 * 订单支付同步URL地址,需要修改为自己的同步回调地址，公网可以访问的,本同步回调地址只在微信H5支付中使用payUrl时起作用
	 */
	private static String RETURN_URL = "https://www.baidu.com";
	/**
	 * 商户号，正式上线需要修改为自己的商户号
	 */
	private static String MERCHANT_NO = "8080108511299699";
	/**
	 * 商户密钥，正式上线需要修改为自己的商户密钥
	 */
	private static String SECRET = "058240662901";

	// 静态代码块实例化URL
	static {
		try {
			PAY_URL = new URL(PATH + "/payment");
			QUERY_URL = new URL(PATH + "/payment/orderStatus");
			REFUND_URL = new URL(PATH + "/payment/refund");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

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
	 * @param params
	 *            签名参数
	 * @return sign
	 */
	private static String getSign(Map<String, String> params) {

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
	 * @param params
	 *            返回参数
	 * @return
	 */
	public static boolean checkParamSign(Map<String, String> params) {
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
	 */
	public static void pay(Map<String, String> param) {
		Map<String, String> params = new HashMap<>();
		params.put("merchantNo", MERCHANT_NO);//商户号
		params.put("notifyUrl", NOTIFY_URL);//回调
		params.put("returnUrl", RETURN_URL);//回调
		params.put("payMethod", param.get("payMethod"));//支付方式
		params.put("version", "v2.0");//版本固定值
		params.put("name", param.get("name"));
		params.put("orderNo", param.get("orderNo"));
		params.put("total", param.get("total"));
		params.put("timestamp", String.valueOf(System.currentTimeMillis()));
		params.put("sign", getSign(params));
		// ************订单生成，当返回result中code=1时，代表订单生成成功，需要验签************
		String result = sendPostMessage(PAY_URL, params);
		System.out.println(result);

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
					System.out.println("签名验证通过,可以在此处理订单下一步操作:");

					// 获取payUrl
					String payUrl = resultObj.get("payUrl");
					System.out.println("payUrl=" + payUrl);

				} else {
					System.out.println("签名验证失败。。。");
				}
			} else {
				String msg = (String) obj.get("msg");
				System.out.println("失败，原因：" + msg);
			}
		} else {
			System.out.println("服务器连接异常，请重试！");
		}
	}

	/**
	 * 测试订单查询，当返回result中status=1时，代表支付成功，需要验签
	 */
	@SuppressWarnings("unused")
	public static void orderQuery(Map<String, String> param) {

		Map<String, String> params = new HashMap<>();
		params.put("merchantNo", MERCHANT_NO);
		params.put("orderNo", param.get("orderNo"));
		params.put("timestamp", String.valueOf(System.currentTimeMillis()));
		params.put("sign", getSign(params));
		// ************订单查询，当返回result中status=1时，代表支付成功，需要验签************
		// status状态：0:等待支付；1：支付成功；2：支付失败；3：订单已撤销；4：订单已退款
		String result = sendPostMessage(QUERY_URL, params);

		System.out.println(result);
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
					System.out.println("签名验证通过");

					// 获取status
					String status = resultObj.get("status");
					System.out.println("status=" + status);
					if ("1".equals(status)) {
						System.out.println("订单支付成功");
					}

				} else {
					System.out.println("签名验证失败。。。");
				}
			} else {
				String msg = (String) obj.get("msg");
				System.out.println("失败，原因：" + msg);
			}
		} else {
			System.out.println("服务器连接异常，请重试！");
		}
	}

	/**
	 * 测试订单退款，全额退款，简单接口，无须验签
	 */
	@SuppressWarnings("unused")
	public static void orderRefund(Map<String, String> param) {

		Map<String, String> params = new HashMap<>();
		params.put("merchantNo", MERCHANT_NO);
		params.put("orderNo", param.get("orderNo"));//单号
		params.put("refundFee", param.get("total"));//退款金额
		params.put("refundReson", param.get("refundReson"));//退款原因
		params.put("timestamp", String.valueOf(System.currentTimeMillis()));
		params.put("sign", getSign(params));
		
		// ************订单退款，全额退款，简单接口，无须验签************
		String result = sendPostMessage(REFUND_URL, params);
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

	public static void main(String[] args) {
		String orderNo = "2019081455909";
		String total = "10";
		String payMethod = "6023";
		String name="绿茶";//商品名称
		String refundReson="协商退款";
		Map<String,String> param = new HashMap<>();
		param.put("orderNo", orderNo);
		param.put("total", total);
		param.put("payMethod", payMethod);
		param.put("name", name);
		param.put("refundReson", refundReson);
		// ************订单生成，当返回result中code=1时，代表订单生成成功，需要验签************
		pay(param);

		// ************订单查询，当返回result中status=1时，代表支付成功，需要验签************
//		orderQuery(param);

		// ************订单退款，全额退款，简单接口，无须验签************
//		orderRefund(param);

	}

}
