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
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

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
import com.dl.shop.payment.pay.apay.util.APayH5Utils;
import com.dl.shop.payment.web.PaymentController;

import lombok.extern.slf4j.Slf4j;
import net.sf.json.util.JSONUtils;
@Slf4j
@Service
public class APayService {
	private final static Logger logger = LoggerFactory.getLogger(PaymentController.class);
	@Resource
	private APayH5Utils autil;
	
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
	public String getSign(Map<String, Object> params) {

		// 先将参数以其参数名的字典序升序进行排序
		Map<String, Object> sortedParams = new TreeMap<String, Object>(params);
		Set<Entry<String, Object>> entrys = sortedParams.entrySet();

		// 遍历排序后的字典，将所有参数按"key=value"格式拼接在一起
		StringBuilder basestring = new StringBuilder();
		for (Entry<String, Object> param : entrys) {
			basestring.append(param.getKey()).append("=").append(param.getValue()).append("&");
		}
		basestring.append("key="+autil.getSECRET());
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
	 * @param params 返回参数
	 * @return
	 */
	public boolean checkParamSign(Map<String, Object> params) {
		boolean result = false;
		try {
			if (params == null || params.size() == 0) {
				return result;
			}
			if (params.containsKey("sign")) {
				String sign = params.get("sign").toString();
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
	public String pay(Map<String, Object> param) throws MalformedURLException {
		Map<String, Object> params = new HashMap<>();
		params.put("partner_id", autil.getMERCHANT_NO());// 商户编号
		params.put("channel_id", param.get("channel_id"));//渠道编号
		params.put("partner_order", param.get("partner_order"));// 商户订单
		params.put("user_id", param.get("user_id"));// 用户id
		params.put("total_fee", param.get("total_fee"));// 订单金额
		params.put("back_url", autil.getNOTIFY_URL());//同步回调地址
		params.put("notify_url", autil.getRETURN_URL());// 异步回调地址
		params.put("payextra_param", "");// 扩展参数
		params.put("pay_method", "");// 支付类型
		params.put("exter_invoke_ip", param.get("exter_invoke_ip"));// 用户ip
		params.put("sign_type", "MD5");// 签名类型
		params.put("sign", getSign(params));
		params.put("subject", param.get("subject"));// 用户id
		logger.info("艾支付接口参数：params={}"+JSONUtils.valueToString(params));
		// ************订单生成，当返回result中code=1时，代表订单生成成功，需要验签************
		String result = sendPostMessage(new URL(autil.getPATH()+autil.getPAY_URL_METHOD()), params);
		return result;
		// 校验返回值
//		if (result != null && !"".equals(result)) {
//			if (!result.contains("code")) {
//				return result;
//			}
//			else {
//				Map<String, Object> obj = JSONObject.parseObject(result, Map.class);
//				return null;
//			}
//		}
//		return null;
	}

	/**
	 * 测试订单查询，当返回result中status=1时，代表支付成功，需要验签
	 * @throws MalformedURLException 
	 */
//	public Map<String, Object> orderQuery(String orderNo) throws MalformedURLException {
//		Map<String, Object> params = new HashMap<>();
//		params.put("merchantNo", autil.getMERCHANT_NO());
//		params.put("orderNo", orderNo);
//		params.put("timestamp", String.valueOf(System.currentTimeMillis()));
//		params.put("sign", getSign(params));
//		// ************订单查询，当返回result中status=1时，代表支付成功，需要验签************
//		// status状态：0:等待支付；1：支付成功；2：支付失败；3：订单已撤销；4：订单已退款
//		logger.info("METHOD ORDERQUERY()华移支付:QUERY_URL={}",autil.getPATH() + autil.getQUERY_URL_METHOD());
//		logger.info("METHOD ORDERQUERY()华移支付:orderNo={}",orderNo);
//		String result = sendPostMessage(new URL(autil.getPATH() + autil.getQUERY_URL_METHOD()), params);
//		logger.info("METHOD ORDERQUERY()华移支付订单查询请求结果:result={}",result);
//		// 校验返回值
//		if (result != null && !"".equals(result)) {
//			@SuppressWarnings("unchecked")
//			Map<String, Object> obj = JSONObject.parseObject(result, Map.class);
//			int code = (int) obj.get("code");
//			if (code == 1)// 成功
//			{
//				@SuppressWarnings("unchecked")
//				Map<String, Object> resultObj = (Map<String, Object>) obj.get("result");
//				// 验证返回参数签名
//				if (checkParamSign(resultObj)) {
//					// 获取status
//					String status = resultObj.get("status").toString();
//					if ("1".equals(status)) {
//						logger.info("METHOD ORDERQUERY()华移支付成功");
//						return resultObj;
//					}else {
//						logger.info("METHOD ORDERQUERY()华移支付失败");
//						return resultObj;
//					}
//				} else {
//					logger.info("METHOD ORDERQUERY()华移支付验证签名失败");
//					return obj;
//				}
//			} else {
//				logger.info("METHOD ORDERQUERY()华移支付订单查询失败");
//				return obj;
//			}
//		} else {
//			logger.info("METHOD ORDERQUERY()华移支付订单查询失败");
//			return null;
//		}
//	}

	/**
	 * 测试订单退款，全额退款，简单接口，无须验签
	 * @throws MalformedURLException 
	 */
//	public void orderRefund(Map<String, Object> param) throws MalformedURLException {
//		Map<String, Object> params = new HashMap<>();
//		params.put("merchantNo", autil.getMERCHANT_NO());
//		params.put("orderNo", param.get("orderNo"));// 单号
//		params.put("refundFee", param.get("total"));// 退款金额
//		params.put("refundReson", param.get("refundReson"));// 退款原因
//		params.put("timestamp", String.valueOf(System.currentTimeMillis()));
//		params.put("sign", getSign(params));
//
//		// ************订单退款，全额退款，简单接口，无须验签************
//		logger.info("华移支付:REFUND_URL={}",autil.getPATH() + autil.getREFUND_URL_METHOD());
//		String result = sendPostMessage(new URL(autil.getPATH() + autil.getREFUND_URL_METHOD()), params);
//		System.out.println(result);
//
//		// 校验返回值
//		if (result != null && !"".equals(result)) {
//			@SuppressWarnings("unchecked")
//			Map<String, Object> obj = JSONObject.parseObject(result, Map.class);
//			int code = (int) obj.get("code");
//			if (code == 1)// 成功
//			{
//				System.out.println("退款成功。");
//			} else {
//				String msg = (String) obj.get("msg");
//				System.out.println("失败，原因：" + msg);
//			}
//		} else {
//			System.out.println("服务器连接异常，请重试！");
//		}
//	}
	
	/**
	 * 支付宝企业收款
	 * @param savePayLog 支付日志
	 * @param orderSn 订单编号
	 * @param orderId 订单id
	 * @param userId 用户id
	 * @param channel_id 渠道编号 微信支付(扫码):6     微信支付H5:7	支付宝支付：9
	 * @param paytype 商品名称 支付/充值
	 * @param exter_invoke_ip 用户IP
	 * @return
	 */
	public BaseResult<?> getAPayUrl(PayLog savePayLog, String orderSn,String orderId,Integer userId,String channel_id,String exter_invoke_ip,String paytype) {
		BaseResult<?> payBaseResult = null;
		BigDecimal amtDouble = savePayLog.getOrderAmount();
		BigDecimal bigD = amtDouble.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_EVEN);// 金额转换成分
		Map<String,Object> param = new HashMap<>();
		param.put("partner_order", orderSn);// 商户订单
		param.put("total_fee", bigD.toString());// 订单金额
		param.put("subject", paytype);// 商品名称
		param.put("channel_id", channel_id);//渠道编号
		param.put("user_id", userId);// 用户id
		param.put("exter_invoke_ip", exter_invoke_ip);// 用户ip
		int code = 0;
		try {
			String result = pay(param);
			param = null;
			if (result != null && !"".equals(result)) {
				if (!result.contains("code")) {
					Document doc = Jsoup.parse(result);
					String url = doc.select("iframe").first().attr("src");
					param = new HashMap<>();
					param.put("payUrl", url.trim());
					param.put("orderId", orderId);
					param.put("payLogId", savePayLog.getLogId());
				} else {
					Map<String, Object> obj = JSONObject.parseObject(result, Map.class);
					code = (int) obj.get("code");
					logger.info("接口返回result={}"+result);
//					Map<String, Object> obj = JSONObject.parseObject(result, Map.class);
//					return null;
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		if (param != null) {
			payBaseResult = ResultGenerator.genSuccessResult("succ", param);
		} else {
			if(code==1007) {
				payBaseResult = ResultGenerator.genFailResult("维护中");
			}else {
				payBaseResult = ResultGenerator.genFailResult("艾支付返回数据有误");
			}
		}
		logger.info("加工前返回APP结果 "+JSONUtils.valueToString(payBaseResult));
		return payBaseResult;
	}
	/**
	 * 查询订单状态
	 * @param orderSn
	 * @return
	 */
//	public BaseResult<RspOrderQueryEntity> commonOrderQueryLid(String orderSn){
//		BaseResult<RspOrderQueryEntity> payBaseResult = null;
//		Map<String,Object> param = null;
//		try {
//			param = orderQuery(orderSn);
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		}
//		if (param != null) {
//			RspOrderQueryEntity rspOrderQueryEntity = new RspOrderQueryEntity();
//			rspOrderQueryEntity.setResult_code(param.get("status")==null?"":param.get("status").toString());
//			rspOrderQueryEntity.setType(RspOrderQueryEntity.TYPE_LID);
//			payBaseResult = ResultGenerator.genSuccessResult("succ", rspOrderQueryEntity);
//		} else {
//			payBaseResult = ResultGenerator.genFailResult("华移支付返回数据有误");
//		} 
//		return payBaseResult;
//	}
	
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
