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
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.dl.base.util.DateUtil;

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
	private static String MERCHANT_NO = "8006859000120561";
	/**
	 * 商户密钥，正式上线需要修改为自己的商户密钥
	 */
	private static String SECRET = "103670529156";

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
//			System.out.println("支付下单URL=" + url.toString() + "?" + stringBuilder.toString());
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
	
	public static void getUserid() {
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
		request.setCode("12c67e5dbb2b455fa703afb0ccdeWX26");
		AlipaySystemOauthTokenResponse response;
		try {
			response = alipayClient.execute(request);
			if(response.isSuccess()){
				System.out.println("调用成功:"+response.getUserId());
				} else {
				System.out.println("调用失败");
				}
		} catch (AlipayApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
//		String orderNo = "42531097";
//		String total = "10";
//		String payMethod = "6023";
//		String name="绿茶";//商品名称
//		String refundReson="协商退款";
		Map<String,String> param = new HashMap<>();
//		param.put("orderNo", orderNo);
//		param.put("total", total);
//		param.put("payMethod", payMethod);
//		param.put("name", name);
////		param.put("refundReson", refundReson);
		// ************订单生成，当返回result中code=1时，代表订单生成成功，需要验签************
		getUserid();
		// ************订单查询，当返回result中status=1时，代表支付成功，需要验签************
//		orderQuery(param);

		// ************订单退款，全额退款，简单接口，无须验签************
//		orderRefund(param);

	}

}
