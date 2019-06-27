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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Resource;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.pay.jhpay.JhPayZFBUtils;
import com.dl.shop.payment.pay.jhpay.util.JsonUtil;
import com.dl.shop.payment.pay.jhpay.util.MD5;
import com.dl.shop.payment.pay.jhpay.util.SignUtils;
import com.dl.shop.payment.pay.jhpay.util.XmlUtils;
import com.dl.shop.payment.web.PaymentController;

import lombok.extern.slf4j.Slf4j;
import net.sf.json.util.JSONUtils;

@Slf4j
@Service
public class JhPayService {
	private final static Logger logger = LoggerFactory.getLogger(PaymentController.class);
	@Resource
	private JhPayZFBUtils zfbutil;
	
	
	/**
	 * 发送消息体到服务端
	 * 
	 * @param params
	 * @return
	 */
	@SuppressWarnings("finally")
	public String jhAliPay(SortedMap<String, String> map){
		
		map.put("service", zfbutil.getPAY_URL());
		map.put("mch_id", zfbutil.getMERCHANT_NO());
		map.put("mch_create_ip", "127.0.0.1");
		map.put("notify_url", zfbutil.getNOTIFY_URL());
		map.put("nonce_str", String.valueOf(new Date().getTime()));
		
		Map<String, String> params = SignUtils.paraFilter(map);
		StringBuilder buf = new StringBuilder((params.size() + 1) * 10);
		SignUtils.buildPayParams(buf, params, false);
		String preStr = buf.toString();
		String sign = MD5.sign(preStr, "&key=" + zfbutil.getSECRET(), "utf-8");
		map.put("sign", sign);

		String reqUrl = zfbutil.getPATH();
		System.out.println("reqUrl：" + reqUrl);
		CloseableHttpResponse response = null;
		CloseableHttpClient client = null;
//		String res = null;
		String tradeNO=null;
		Map<String, String> resultMap = null;
		try {
			HttpPost httpPost = new HttpPost(reqUrl);
			StringEntity entityParams = new StringEntity(XmlUtils.parseXML(map), "utf-8");
			httpPost.setEntity(entityParams);
			client = HttpClients.createDefault();
			response = client.execute(httpPost);
			if (response != null && response.getEntity() != null) {
				resultMap = XmlUtils.toMap(EntityUtils.toByteArray(response.getEntity()), "utf-8");
//				res = XmlUtils.toXml(resultMap);

				if (resultMap.containsKey("sign")) {
					if (!SignUtils.checkParam(resultMap, zfbutil.getSECRET())) {
//						res = "验证签名不通过";
					} else {
						if ("0".equals(resultMap.get("status")) && "0".equals(resultMap.get("result_code"))) {
//							Map<String, String> orderResult = new HashMap<String, String>(); // 用来存储订单的交易状态(key:订单号，value:状态(0:未支付，1：已支付)) ---- 这里可以根据需要存储在数据库中
//							orderResult.put(map.get("out_trade_no"), "0");// 初始状态
							String pay_info = resultMap.get("pay_info");
							Map payInfo = JsonUtil.jsonToMap(pay_info);
							tradeNO = (String) payInfo.get("tradeNO");
//							String code_img_url = resultMap.get("code_img_url");
						}
					}
				}
			} else {
//				res = "操作失败";
			}
		} catch (Exception e) {
			e.printStackTrace();
//			res = "系统异常";
		} finally {
			try {
				if (response != null) {
					response.close();
				}
				if (client != null) {
					client.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			return tradeNO;
		}
	}

	
	/**支付宝支付
	 * @param savePayLog 支付日志
	 * @param orderSn 订单编号
	 * @param orderId 订单id
	 * @param payway 支付方式 1:支付宝
	 * @param paytype 商品名称 支付/充值
	 * @return
	 */
	public BaseResult<?> getZFBPayUrl(PayLog savePayLog, String orderSn,String orderId,String paytype,String payUserId) {
		BaseResult<?> payBaseResult = null;
		BigDecimal amtDouble = savePayLog.getOrderAmount();
		BigDecimal bigD = amtDouble.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_EVEN);// 金额转换成分
		SortedMap<String,String> param = new TreeMap<>();
		param.put("out_trade_no", orderSn);
		param.put("body", paytype);
		param.put("total_fee", bigD.toString());
		param.put("buyer_id", payUserId);
//		Map<String,Object> resultMap = null;
		String result = null;
		try {
//			Map<String,Object> result = jhAliPay(param);
//			if (result != null) {
//				resultMap = new HashMap<>();
//				resultMap.put("payUrl", result.get("pay_url"));
//				resultMap.put("orderId", orderId);
//				resultMap.put("payLogId", savePayLog.getLogId());
//			}
			result = jhAliPay(param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (result != null) {
			payBaseResult = ResultGenerator.genSuccessResult("succ", result);
		} else {
			payBaseResult = ResultGenerator.genFailResult("聚合支付返回数据有误");
		}
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
