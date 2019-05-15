package com.dl.shop.payment.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

import javax.annotation.Resource;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.pay.jhpay.JhPayWXUtils;
import com.dl.shop.payment.pay.jhpay.JhPayZFBUtils;
import com.dl.shop.payment.web.PaymentController;

import lombok.extern.slf4j.Slf4j;
import net.sf.json.util.JSONUtils;

@Slf4j
@Service
public class JhPayService {
	private final static Logger logger = LoggerFactory.getLogger(PaymentController.class);
	@Resource
	private JhPayWXUtils autil;
	@Resource
	private JhPayZFBUtils zfbutil;
	
	
	/**
	 * 发送消息体到服务端
	 * 
	 * @param params
	 * @return
	 */
	public String sendPostMessage(URL url, Map<String, Object> params) {
		StringBuilder stringBuilder = new StringBuilder();
		if (params != null && !params.isEmpty()) {
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				try {
					stringBuilder.append(entry.getKey()).append("=")
							.append(URLEncoder.encode(entry.getValue()==null?"":entry.getValue().toString(), "utf-8")).append("&");
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
	public String getSign(StringBuilder basestring) {
		basestring.append(autil.getSECRET());
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
	/**微信支付
	 * @throws MalformedURLException 
	 */
	public String wxpay(Map<String, Object> param) throws MalformedURLException {
		Map<String, Object> params = new HashMap<>();
		params.put("out_trade_no", param.get("out_trade_no"));// 商户订单
		StringBuilder basestring = new StringBuilder();
		basestring.append(params.get("out_trade_no")).append("&key=");
		params.put("checksign", getSign(basestring));//签名只订单号+key  大写
		params.put("payway", "WX");// WX:微信支付
		params.put("total_fee", param.get("total_fee"));// 订单金额
		params.put("body", param.get("body"));// 订单描述
		Map<String, Object> jhSh = new HashMap<>();
		jhSh.put("user_id", autil.getMERCHANT_NO());// 商户ID
		jhSh.put("user_name", autil.getMERCHANT_NAME());// 商户名
		jhSh.put("notify_url", autil.getNOTIFY_URL());// 异步回调地址
		params.put("attach", JSONUtils.valueToString(jhSh));//自定义参数
		
		logger.info("聚合支付接口参数：params={}"+JSONUtils.valueToString(params));
		// ************订单生成，当返回result中code=1时，代表订单生成成功，需要验签************
		String result = sendPostMessage(new URL(autil.getPATH()+autil.getPAY_URL_METHOD()), params);
		return result;
	}

	/**支付宝支付
	 * @throws MalformedURLException 
	 */
	public String zfbpay(Map<String, Object> param) throws MalformedURLException {
		Map<String, Object> params = new HashMap<>();
		params.put("out_trade_no", param.get("out_trade_no"));// 商户订单
		params.put("price", param.get("price"));// 订单金额
		params.put("istype", "1");// 支付方式(1: 支付宝扫码)
		params.put("adduid", zfbutil.getADDUID());// 商户id(你们定)
		params.put("adduser", zfbutil.getMERCHANT_NAME());//商户网站的用户名(pankou28，不可修改)
		params.put("mct_id", zfbutil.getMERCHANT_NO());//商户id(153,不可修改)
		StringBuilder basestring = new StringBuilder();
		basestring.append(params.get("out_trade_no")).append(params.get("price")).append(params.get("istype"))
				.append(zfbutil.getADDUID()).append(zfbutil.getMERCHANT_NAME()).append(zfbutil.getMERCHANT_NO());
		params.put("sign", getSign(basestring).toLowerCase());//签名 小写
		params.put("goodsname", param.get("goodsname"));//商品名(你们定, 不可用中文)
		params.put("adddata", param.get("goodsname"));//附加信息(你们定)
		params.put("notify_url", zfbutil.getNOTIFY_URL());//成功支付后的异步通知地址
		params.put("return_url", zfbutil.getNOTIFY_URL());//成功支付后的异步通知地址
		logger.info("聚合支付接口参数：params={}"+JSONUtils.valueToString(params));
		// ************订单生成，当返回result中code=1时，代表订单生成成功，需要验签************
		String result = sendPostMessage(new URL(zfbutil.getPATH()+zfbutil.getPAY_URL_METHOD()), params);
		return result;
	}
	
	/**微信企业收款
	 * @param savePayLog 支付日志
	 * @param orderSn 订单编号
	 * @param orderId 订单id
	 * @param payway 支付方式 WX:微信
	 * @param paytype 商品名称 支付/充值
	 * @return
	 */
	public BaseResult<?> getWXPayUrl(PayLog savePayLog, String orderSn,String orderId,String payway,String paytype) {
		BaseResult<?> payBaseResult = null;
		BigDecimal amtDouble = savePayLog.getOrderAmount();
		BigDecimal bigD = amtDouble.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_EVEN);// 金额转换成分
		Map<String,Object> param = new HashMap<>();
		param.put("out_trade_no", orderSn);// 商户订单
		param.put("payway", payway);// WX:微信支付
		param.put("total_fee", bigD);// 订单金额
		param.put("body", paytype);// 订单描述
		try {
			String result = wxpay(param);
			param = null;
			if (result != null && !"".equals(result)) {
				if (!result.contains("code")) {
					Document doc = Jsoup.parse(result);
					String url = doc.select("iframe").first().attr("src");
					param = new HashMap<>();
					param.put("payUrl", url.trim());
					param.put("orderId", orderId);
					param.put("payLogId", savePayLog.getLogId());
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		if (param != null) {
			payBaseResult = ResultGenerator.genSuccessResult("succ", param);
		} else {
			payBaseResult = ResultGenerator.genFailResult("聚合支付返回数据有误");
		}
		logger.info("加工前返回APP结果 "+JSONUtils.valueToString(payBaseResult));
		return payBaseResult;
	}
	
	/**支付宝企业收款
	 * @param savePayLog 支付日志
	 * @param orderSn 订单编号
	 * @param orderId 订单id
	 * @param payway 支付方式 1:支付宝
	 * @param paytype 商品名称 支付/充值
	 * @return
	 */
	public BaseResult<?> getZFBPayUrl(PayLog savePayLog, String orderSn,String orderId,String paytype) {
		BaseResult<?> payBaseResult = null;
		BigDecimal amtDouble = savePayLog.getOrderAmount();
//		BigDecimal bigD = amtDouble.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_EVEN);// 金额转换成分
		Map<String,Object> param = new HashMap<>();
		param.put("out_trade_no", orderSn);// 商户订单
		param.put("price", amtDouble);// 订单金额 单位元
		param.put("goodsname", paytype);//商品名(你们定, 不可用中文)
		
		try {
			String result = zfbpay(param);
			param = null;
			if (result != null && !"".equals(result)) {
				if (!result.contains("code")) {
					Document doc = Jsoup.parse(result);
					String url = doc.select("iframe").first().attr("src");
					param = new HashMap<>();
					param.put("payUrl", url.trim());
					param.put("orderId", orderId);
					param.put("payLogId", savePayLog.getLogId());
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		if (param != null) {
			payBaseResult = ResultGenerator.genSuccessResult("succ", param);
		} else {
			payBaseResult = ResultGenerator.genFailResult("聚合支付返回数据有误");
		}
		logger.info("加工前返回APP结果 "+JSONUtils.valueToString(payBaseResult));
		return payBaseResult;
	}
	
	public boolean checkMinAmount(String payToken,String paytype) {
		JSONObject josn = (JSONObject) JSONObject.parse(payToken);
		BigDecimal thirdPartyPaid = new BigDecimal(josn.getString("thirdPartyPaid"));
		int paid = thirdPartyPaid.intValue();
		if("6".equals(paytype) && paid<500) {
			return true;
		}
		if("7".equals(paytype) && paid<10) {
			return true;
		}
		if(paid<1) {
			return true;
		}
		return false;
	}
	public boolean checkMaxAmount(String payToken,String paytype) {
		JSONObject josn = (JSONObject) JSONObject.parse(payToken);
		BigDecimal thirdPartyPaid = new BigDecimal(josn.getString("thirdPartyPaid"));
		int paid = thirdPartyPaid.intValue();
		if("7".equals(paytype) && paid>300) {
			return true;
		}
		if(paid>10000) {
			return true;
		}
		return false;
	}
}
