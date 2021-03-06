package com.dl.shop.payment.pay.yunpay;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
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
public class YunPayDemo {
	
	/**
	 * 服务端地址 
	 */
	private static String PATH = "http://tfpay1688.com/alipay";
	/**
	 * 订单支付URL
	 */
	private String PAY_URL_METHOD = "/transferJson";
	/**
	 * 订单查询URL
	 */
	private String QUERY_URL_METHOD = "/queryOrder";
	/**
	 * 订单支付回调URL地址,需要修改为自己的异步回调地址，公网可以访问的
	 */
	private String NOTIFY_URL = "https://www.baidu.com";
	/**
	 * 商户号，正式上线需要修改为自己的商户号
	 */
	private String MERCHANT_NO = "26061708";
	/**
	 * 商户密钥，正式上线需要修改为自己的商户密钥
	 */
	private String SECRET = "nCVp6esqwSVFv89UMZa4u63AxDv89cBq";

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
			System.out.println("sendPostMessage():支付下单URL=" + url.toString() + "?" + stringBuilder.toString());
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
	private String getSign(String basestring) {
//		签名规则md5(商户号+秘钥+订单金额+商户订单号+发起交易时间) . toUpperCase
		System.out.println("getSign():sign"+basestring);
		// 使用MD5对待签名串求签
		byte[] bytes = null;
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("MD5");
			bytes = md5.digest(basestring.getBytes("UTF-8"));
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
	 * 验签，验证返回参数签名。异步回调时，将所有post参数放入Map集合中，调用该方法即可进行参数验证
	 * 
	 * @param params
	 *            返回参数
	 * @return
	 */
	public boolean checkParamSign(Map<String, String> params) {
//		签名MD5(商户号+秘钥+商户订单号+平台订单号+订单金额+系统交易时间). toUpperCase
		boolean result = false;
		try {
			if (params == null || params.size() == 0) {
				return result;
			}
			if (params.containsKey("sign")) {
				String sign = params.get("sign");
				params.remove("sign");
				String signRecieve = getSign(params.get("checksign"));
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
		params.put("sign", getSign(MERCHANT_NO+SECRET+param.get("tran_amt")+param.get("sp_billno")+param.get("tran_time")));
		params.put("spid", MERCHANT_NO);// 商户编号
		params.put("notify_url", NOTIFY_URL);// 异步回调地址
		params.put("pay_type", "alipay");// 支付类型：支付宝-alipay 微信-wx 云闪付-yunshanfu
		params.put("sp_billno", param.get("sp_billno"));// 商户订单
//		params.put("pay_type", param.get("pay_type"));// 支付类型：支付宝-alipay 微信-wx 云闪付-yunshanfu
		params.put("tran_time", param.get("tran_time"));// 发起交易时间
		params.put("tran_amt", param.get("tran_amt"));// 订单金额
		params.put("cur_type", "CNY");// 币种类型
		params.put("item_name", param.get("item_name"));//商品描述
		params.put("item_attach", "");//商品附加数据
		// ************订单生成，当返回result中code=1时，代表订单生成成功，需要验签************
		String result = sendPostMessage(new URL(PATH+PAY_URL_METHOD), params);
		System.out.println("result="+result);
		// 校验返回值
		if (result != null && !"".equals(result)) {
			System.out.println(result);
			@SuppressWarnings("unchecked")
			Map<String, Object> obj = JSONObject.parseObject(result, Map.class);
			String code = (String) obj.get("statusCode");
			if ("0".equals(code))// 成功
			{
				@SuppressWarnings("unchecked")
				Map<String,String> checkMap = new HashMap<>();
				checkMap.put("sign", obj.get("sign").toString());
				checkMap.put("checksign", MERCHANT_NO+SECRET+obj.get("sp_billno")+obj.get("listid").toString()+obj.get("tran_amt")+obj.get("transferDate"));
				// 验证返回参数签名
				if (checkParamSign(checkMap)) {
					System.out.println("签名验证通过,可以在此处理订单下一步操作:");

					// 获取payUrl
					String payUrl = obj.get("aliPayUrl").toString();
					System.out.println("payUrl=" + payUrl);

				} else {
					System.out.println("签名验证失败。。。");
				}
			} else {
				String msg = (String) obj.get("statusMsg");
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
		params.put("sign", getSign(""));
		// ************订单查询，当返回result中status=1时，代表支付成功，需要验签************
		// status状态：0:等待支付；1：支付成功；2：支付失败；3：订单已撤销；4：订单已退款
		String result = sendPostMessage(new URL(PATH+QUERY_URL_METHOD), params);
		System.out.println("result="+result);
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


	public static void main(String[] args) throws MalformedURLException {
		YunPayDemo apay = new YunPayDemo();
		Map<String,String> param = new HashMap<>();
//		签名规则md5(商户号+秘钥+订单金额+商户订单号+发起交易时间) . toUpperCase
		param.put("sp_billno", "232SZZSSZNNHF");// 商户订单
		long nowTime=System.currentTimeMillis();//取得当前系统时间戳
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
		String date = sdf.format(new Date(nowTime));
		param.put("tran_time", date);// 发起交易时间
		param.put("tran_amt", "10");// 订单金额
		param.put("item_name", "支付");//商品描述
		// ************订单生成，当返回result中code=1时，代表订单生成成功，需要验签************
		apay.pay(param);

		// ************订单查询，当返回result中status=1时，代表支付成功，需要验签************
//		apay.orderQuery(param);

		// ************订单退款，全额退款，简单接口，无须验签************
//		apay.orderRefund(param);
		
	}

}
