package com.dl.shop.payment.pay.apay.util;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.alibaba.fastjson.JSONObject;
import com.dl.base.util.DateUtil;
import com.dl.shop.payment.utils.DateUtilPay;

/**
 * 华移支付工具类 。 由于需要解析返回的json数据，本工具类需要引入fastjson.jar第三方包
 * 
 * @author
 *
 */
public class APayDemo {
	
	/**
	 * 服务端地址 
	 */
	private static String PATH = "http://pay1.payurl.club:9191/cgi-bin";
	/**
	 * 订单支付URL
	 */
	private String PAY_URL = "/cashier.do";
	/**
	 * 订单查询URL
	 */
	private String QUERY_URL = "/orderquery.do";
	/**
	 * 订单退款URL
	 */
	private String REFUND_URL;
	/**
	 * 订单支付回调URL地址,需要修改为自己的异步回调地址，公网可以访问的
	 */
	private String NOTIFY_URL = "https://www.baidu.com";
	/**
	 * 订单支付同步URL地址,需要修改为自己的同步回调地址，公网可以访问的,本同步回调地址只在微信H5支付中使用payUrl时起作用
	 */
	private String RETURN_URL = "https://www.baidu.com";
	/**
	 * 商户号，正式上线需要修改为自己的商户号
	 */
	private String MERCHANT_NO = "11880031";
	/**
	 * 商户密钥，正式上线需要修改为自己的商户密钥
	 */
	private String SECRET = "e1f88b0d13031f99acd6dbce553ded1f";

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
	private String getSign(Map<String, String> params) {

		// 先将参数以其参数名的字典序升序进行排序
		Map<String, String> sortedParams = new TreeMap<String, String>(params);
		Set<Entry<String, String>> entrys = sortedParams.entrySet();

		// 遍历排序后的字典，将所有参数按"key=value"格式拼接在一起
		StringBuilder basestring = new StringBuilder();
		for (Entry<String, String> param : entrys) {
			basestring.append(param.getKey()).append("=").append(param.getValue()).append("&");
		}
		basestring.append("key="+SECRET);
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
		return sign.toString();
	}

	/**
	 * 验签，验证返回参数签名。异步回调时，将所有post参数放入Map集合中，调用该方法即可进行参数验证
	 * 
	 * @param params
	 *            返回参数
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
	public void pay(Map<String, String> param) throws MalformedURLException {
		Map<String, String> params = new HashMap<>();
		params.put("partner_id", param.get("partner_id"));// 商户编号
		params.put("channel_id", param.get("channel_id"));//渠道编号
		params.put("partner_order", param.get("partner_order"));// 商户订单
		params.put("user_id", param.get("user_id"));// 用户id
		params.put("total_fee", param.get("total_fee"));// 订单金额
		params.put("back_url", param.get("back_url"));//同步回调地址
		params.put("notify_url", param.get("notify_url"));// 异步回调地址
		params.put("payextra_param", param.get("payextra_param"));// 扩展参数
		params.put("pay_method", param.get("pay_method"));// 支付类型
		params.put("exter_invoke_ip", param.get("exter_invoke_ip"));// 用户ip
		params.put("sign_type", param.get("sign_type"));// 签名类型
		params.put("sign", getSign(params));
		params.put("subject", param.get("sign_type"));// 用户id
		// ************订单生成，当返回result中code=1时，代表订单生成成功，需要验签************
		String result = sendPostMessage(new URL(PATH+PAY_URL), params);
		System.out.println(result);

		// 校验返回值
		if (result != null && !"".equals(result)) {
			if (!result.contains("code")) {
				
				Document doc = Jsoup.parse(result);
				String url = doc.select("iframe").first().attr("src");
				System.out.println(url);
				return;
			}
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
	 * @throws MalformedURLException 
	 */
	public void orderQuery(Map<String, String> param) throws MalformedURLException {

		Map<String, String> params = new HashMap<>();
		params.put("partner_id", MERCHANT_NO);
		params.put("partner_order", param.get("partner_order"));
		params.put("sign", getSign(params));
		// ************订单查询，当返回result中status=1时，代表支付成功，需要验签************
		// status状态：0:等待支付；1：支付成功；2：支付失败；3：订单已撤销；4：订单已退款
		String result = sendPostMessage(new URL(PATH+QUERY_URL), params);

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
	 * @throws MalformedURLException 
	 */
	public void orderRefund(Map<String, String> param) throws MalformedURLException {

		Map<String, String> params = new HashMap<>();
		params.put("sign", getSign(params));
		
		// ************订单退款，全额退款，简单接口，无须验签************
		String result = sendPostMessage(new URL(PATH+REFUND_URL), params);
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

	public static void main(String[] args) throws MalformedURLException {
		APayDemo apay = new APayDemo();
		Map<String,String> param = new HashMap<>();
//		{partner_id=11880031, 
//		partner_order=2019041716395561300016, 
//		payextra_param=, 
//		user_id=1000000025, 
//		subject=支付, 
//		total_fee=1000, 
//		back_url=http://39.106.18.39:8765/api/payment/payment/notify/APayNotify, 
//		sign=17cca74344c23dbc6641ae236b31ecba, 
//		exter_invoke_ip=127.0.0.1, 
//		notify_url=http://39.106.18.39:8765/api/payment/payment/notify/APayNotify, 
//		channel_id=9, 
//		sign_type=MD5}" 
		param.put("partner_id", "11880031");// 商户编号
		param.put("back_url", "http://39.106.18.39:8765/api/payment/payment/notify/APayNotify");//同步回调地址
		param.put("notify_url", "http://39.106.18.39:8765/api/payment/payment/notify/APayNotify");// 异步回调地址
		param.put("channel_id", "7");//渠道编号
		param.put("pay_method", "");// 支付类型
		param.put("partner_order", "2115561300016");// 商户订单
		param.put("user_id", "1000000025");// 用户id
		param.put("total_fee", "1000");// 订单金额
		param.put("payextra_param", "");// 扩展参数
		param.put("exter_invoke_ip", "127.0.0.1");// 用户ip
		param.put("sign_type", "MD5");// 签名类型
		param.put("subject", "支付");// 用户id
		// ************订单生成，当返回result中code=1时，代表订单生成成功，需要验签************
		apay.pay(param);

		// ************订单查询，当返回result中status=1时，代表支付成功，需要验签************
//		apay.orderQuery(param);

		// ************订单退款，全额退款，简单接口，无须验签************
//		apay.orderRefund(param);
		
		//比赛提前1h	禁止支付
		Integer sysLimitBetTime = 3600;
//		SysConfigParam sysConfigParam = new SysConfigParam();
//		sysConfigParam.setBusinessId(4);
//		BaseResult<SysConfigDTO> sysConfigDTOBaseResult = iSysConfigService.querySysConfig(sysConfigParam);
//		if(sysConfigDTOBaseResult.isSuccess()){
//			sysLimitBetTime = sysConfigDTOBaseResult.getData().getValue().intValue();
//		}

//		Integer nowTime = DateUtil.getCurrentTimeLong();
//		Integer matchTime = DateUtil.getCurrentTimeLong();
//		String s= DateUtil.getTimeString(DateUtil.getCurrentTimeLong(), DateUtil.datetimeFormat);
//		System.out.println(Integer.valueOf(DateUtilPay.dateSubtractionHours("2019-04-19 15:05:00","2050-04-22 16:05:00")));
//		System.out.println(nowTime);
//		System.out.println(DateUtil.getTimeSomeDate(DateUtil.strToDate("2019-04-19 16:05:00")));
//		System.out.println(DateUtil.getTimeSomeDate(DateUtil.strToDate("2019-04-19 15:05:00")));
//		System.out.println(DateUtil.getTimeSomeDate(new Date()));
//		System.out.println(matchTime - sysLimitBetTime  <= nowTime);
//		if(min.getMatchTime() - sysLimitBetTime  <= nowTime){
//			return ResultGenerator.genResult(LotteryResultEnum.BET_TIME_LIMIT.getCode(), LotteryResultEnum.BET_TIME_LIMIT.getMsg());
//		}
//		String surplusStr ="";
//		surplusStr = surplusStr.substring(0, surplusStr.length()-1);
//		System.out.println(surplusStr);
	}

}
