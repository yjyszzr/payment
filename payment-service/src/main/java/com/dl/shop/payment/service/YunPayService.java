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
import java.text.SimpleDateFormat;
import java.util.Date;
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
import com.dl.shop.payment.pay.yunpay.YunPayUtils;
import com.dl.shop.payment.web.PaymentController;

import lombok.extern.slf4j.Slf4j;
import net.sf.json.util.JSONUtils;
@Slf4j
@Service
public class YunPayService {
	private final static Logger logger = LoggerFactory.getLogger(PaymentController.class);
	@Resource
	private YunPayUtils autil;
	
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
	public Map<String,Object> pay(Map<String, String> param) throws MalformedURLException {
		Map<String, String> params = new HashMap<>();
		params.put("sign", getSign(autil.getMERCHANT_NO()+autil.getSECRET()+param.get("tran_amt")+param.get("sp_billno")+param.get("tran_time")));
		params.put("spid", autil.getMERCHANT_NO());// 商户编号
		params.put("notify_url", autil.getNOTIFY_URL());// 异步回调地址
		params.put("sp_billno", param.get("sp_billno"));// 商户订单
		params.put("pay_type", param.get("pay_type"));// 支付类型：支付宝-alipay 微信-wx 云闪付-yunshanfu
		params.put("tran_time", param.get("tran_time"));// 发起交易时间
		params.put("tran_amt", param.get("tran_amt"));// 订单金额
		params.put("cur_type", "CNY");// 币种类型
		params.put("item_name", param.get("item_name"));//商品描述
		params.put("item_attach", "");//商品附加数据
		logger.info("云闪付支付接口参数：params={}"+JSONUtils.valueToString(params));
		// ************订单生成，当返回result中code=1时，代表订单生成成功，需要验签************
		String result = sendPostMessage(new URL(autil.getPATH()+autil.getPAY_URL_METHOD()), params);
		// 校验返回值
		if (result != null && !"".equals(result)) {
			@SuppressWarnings("unchecked")
			Map<String, Object> obj = JSONObject.parseObject(result, Map.class);
			String code = (String) obj.get("statusCode");
			if ("0".equals(code))// 成功
			{
				Map<String,String> checkMap = new HashMap<>();
				checkMap.put("sign", obj.get("sign").toString());
				checkMap.put("checksign", autil.getMERCHANT_NO()+autil.getSECRET()+obj.get("sp_billno")+obj.get("listid").toString()+obj.get("tran_amt")+obj.get("transferDate"));
				if(checkParamSign(checkMap)) {
					return obj;
				}else {
					log.info("云闪付支付失败：原因：返回结果验签失败");
					return null;
				}
			} else {
				String msg = (String) obj.get("statusMsg");
				log.info("云闪付支付失败：原因："+msg);
				return null;
			}
		}
		return null;
	}

	/**
	 * 云闪付支付
	 * @param savePayLog 支付日志
	 * @param orderSn 订单编号
	 * @param orderId 订单id
	 * @param userId 用户id
	 * @param channel_id 支付类型：支付宝-alipay 微信-wx 云闪付-yunshanfu
	 * @param paytype 商品名称 支付/充值
	 * @return
	 */
	public BaseResult<?> getYunPayUrl(PayLog savePayLog, String orderSn,String orderId,String channel_id,String paytype) {
		BaseResult<?> payBaseResult = null;
		BigDecimal amtDouble = savePayLog.getOrderAmount();
//		BigDecimal bigD = amtDouble.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_EVEN);// 金额转换成分
		Map<String,String> param = new HashMap<>();
		param.put("sp_billno", orderSn);// 商户订单
		long nowTime=System.currentTimeMillis();//取得当前系统时间戳
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
		String date = sdf.format(new Date(nowTime));
		param.put("tran_time", date);// 发起交易时间
//		param.put("tran_amt", amtDouble.toString());// 订单金额
		param.put("tran_amt", "0.1");// 订单金额
		param.put("item_name", paytype);//商品描述
		param.put("pay_type", channel_id);// 支付类型：支付宝-alipay 微信-wx 云闪付-yunshanfu
		Map<String,Object> resultMap = null;
		try {
			Map<String,Object> result = pay(param);
			if(result!=null && result.get("aliPayUrl")!=null) {
				resultMap = new HashMap<>();
				resultMap.put("payUrl", result.get("aliPayUrl").toString());
				resultMap.put("orderId", orderId);
				resultMap.put("payLogId", savePayLog.getLogId());
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		if (resultMap != null) {
			payBaseResult = ResultGenerator.genSuccessResult("succ", resultMap);
		} else {
			payBaseResult = ResultGenerator.genFailResult("云闪付支付返回数据有误");
		}
		logger.info("云闪付加工前返回APP结果 "+JSONUtils.valueToString(payBaseResult));
		return payBaseResult;
	}
	
	public boolean checkMinAmount(String payToken,String paytype) {
		JSONObject josn = (JSONObject) JSONObject.parse(payToken);
		BigDecimal thirdPartyPaid = new BigDecimal(josn.getString("thirdPartyPaid"));
		int paid = thirdPartyPaid.intValue();
		if(paid<1) {
			return true;
		}
		return false;
	}
	public boolean checkMaxAmount(String payToken,String paytype) {
		JSONObject josn = (JSONObject) JSONObject.parse(payToken);
		BigDecimal thirdPartyPaid = new BigDecimal(josn.getString("thirdPartyPaid"));
		int paid = thirdPartyPaid.intValue();
		if(paid>10000) {
			return true;
		}
		return false;
	}
}
