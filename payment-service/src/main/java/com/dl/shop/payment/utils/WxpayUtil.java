package com.dl.shop.payment.utils;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Resource;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.util.DateUtil;
import com.dl.shop.payment.configurer.WxpayConfig;
import com.dl.shop.payment.dto.WxpayAppDTO;
import com.dl.shop.payment.model.OrderQueryResponse;
import com.dl.shop.payment.model.UnifiedOrderParam;
import com.dl.shop.payment.model.WxpayAppModel;
import com.dl.shop.payment.model.WxpayOrderQuery;
import com.dl.shop.payment.model.WxpayUnifiedOrder;
import com.dl.shop.payment.pay.common.RspOrderQueryEntity;


@Component
public class WxpayUtil {

	private final static Logger logger = LoggerFactory.getLogger(WxpayUtil.class);
	@Resource
	private WxpayConfig wxpayConfig;
	@Resource
	private RestTemplate restTemplate;

	/**
	 * 支付类型
	 */
	private static final String APP_TRADE_TYPE = "APP";
	private static final String JS_TRADE_TYPE = "JSAPI";
	private static final String NATIVE_TRADE_TYPE = "NATIVE";
	private static final String FAIL_RESULT_CODE = "FAIL";


	/**
	 * 统一下单,会自动签名和补上noce_str
	 * @return 下单后返回信息
	 */
	public BaseResult<WxpayAppDTO> unifiedOrderForApp(UnifiedOrderParam param){
		String body = param.getBody();
		Integer orderNo = param.getOrderNo();
		int totalFee = (int)(param.getTotalAmount()*100);
		String ip = param.getIp();
		if(StringUtils.isBlank(body)){
			body = "商品";
		}
		WxpayUnifiedOrder unifiedOrder = new WxpayUnifiedOrder();
		unifiedOrder.setBody(body);
		unifiedOrder.setOut_trade_no(orderNo+"");
		unifiedOrder.setTotal_fee(totalFee);
		unifiedOrder.setSpbill_create_ip(ip);
		unifiedOrder.setAppid(wxpayConfig.getWxAppAppId());
		unifiedOrder.setMch_id(wxpayConfig.getWxAppMchId());
		unifiedOrder.setTrade_type(APP_TRADE_TYPE);
		unifiedOrder.setNotify_url(wxpayConfig.getWxNotifyUrl());
		String nonceStr = nonceStr(16);
		unifiedOrder.setNonce_str(nonceStr);
		logger.info(wxpayConfig.getWxAppAppKey());
		unifiedOrder.setSign(this.sign(this.bean2TreeMap(unifiedOrder), wxpayConfig.getWxAppAppKey()).toUpperCase());
		String tempXmlStr = XmlUtil.beanToXml(new ByteArrayOutputStream(), unifiedOrder);
		String requestXml = tempXmlStr!= null?tempXmlStr.substring(55):"";
		logger.info("xml转义后内容:"+requestXml);
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.parseMediaType("text/xml; charset=UTF-8"));
			HttpEntity<String> formEntity = new HttpEntity<String>(requestXml, headers);
			String resultXml = restTemplate.postForObject(wxpayConfig.getWxUnifiedOrderUrl(), formEntity, String.class);
			resultXml = new String(resultXml.getBytes("iso8859-1"),"utf-8");
			logger.info("微信返回内容:"+resultXml);
			if(StringUtils.isBlank(resultXml)) {
				return ResultGenerator.genFailResult("微信返回内容为空", null);
			}
			WxpayUnifiedOrder.Response response = XmlUtil.xmlToBean(resultXml, WxpayUnifiedOrder.Response.class);
			if(null == response) {
				return ResultGenerator.genFailResult("WxpayUnifiedOrder.Response is null", null);
			}
			String resultCode = response.getResult_code();
			String returnCode = response.getReturn_code();
			if("SUCCESS".equals(resultCode) && "SUCCESS".equals(returnCode)) {
				WxpayAppModel appModel = new WxpayAppModel();
				appModel.setAppid(wxpayConfig.getWxAppAppId());
				appModel.setNoncestr(nonceStr);
				appModel.setPackageValue("Sign=WXPay");
				appModel.setPartnerid(wxpayConfig.getWxAppMchId());
				appModel.setPrepayid(response.getPrepay_id());
				Date ctime = new Date();
				long timestamp = ctime.getTime()/1000;
				appModel.setTimestamp(timestamp+"");
				String signStr = this.sign(this.bean2TreeMap(appModel), wxpayConfig.getWxAppAppKey()).toUpperCase();
				appModel.setSign(signStr);
				WxpayAppDTO dto = this.wXpayAppModel2DTO(appModel);
				return ResultGenerator.genSuccessResult("success", dto);
			}else {
				String msg = "err_code:" + response.getErr_code() + ",err_desc:"+response.getErr_code_des();
				return ResultGenerator.genFailResult(msg, null);
			}
		} catch (Exception e) {
			logger.error("微信app支付失败", e);
			e.printStackTrace();
		}
		return ResultGenerator.genFailResult("微信app支付调起失败", null);
	}
	/**
	 * 查询订单状态
	 * @param param
	 * @return
	 */
	public BaseResult<RspOrderQueryEntity> orderQuery(String orderNo){
		WxpayOrderQuery queryOrder = new WxpayOrderQuery();
		queryOrder.setOut_trade_no(orderNo);
		queryOrder.setAppid(wxpayConfig.getWxAppAppId());
		queryOrder.setMch_id(wxpayConfig.getWxAppMchId());
		String nonceStr = nonceStr(16);
		queryOrder.setNonce_str(nonceStr);
		logger.info(wxpayConfig.getWxAppAppKey());
		queryOrder.setSign(this.sign(this.bean2TreeMap(queryOrder), wxpayConfig.getWxAppAppKey()).toUpperCase());
		String tempXmlStr = XmlUtil.beanToXml(new ByteArrayOutputStream(), queryOrder);
		String requestXml = tempXmlStr!= null?tempXmlStr.substring(55):"";
		logger.info("xml转义后内容:"+requestXml);
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.parseMediaType("text/xml; charset=UTF-8"));
			HttpEntity<String> formEntity = new HttpEntity<String>(requestXml, headers);
			String resultXml = restTemplate.postForObject("https://api.mch.weixin.qq.com/pay/orderquery", formEntity, String.class);
			resultXml = new String(resultXml.getBytes("iso8859-1"),"utf-8");
			logger.info("微信返回内容:"+resultXml);
			if(StringUtils.isBlank(resultXml)) {
				return ResultGenerator.genFailResult("微信返回内容为空", null);
			}
			WxpayOrderQuery.Response response = XmlUtil.xmlToBean(resultXml, WxpayOrderQuery.Response.class);
			if(null == response) {
				return ResultGenerator.genFailResult("WxpayOrderQuery.Response is null", null);
			}
			String resultCode = response.getResult_code();
			String returnCode = response.getReturn_code();
			if("SUCCESS".equals(resultCode) && "SUCCESS".equals(returnCode)) {
				RspOrderQueryEntity queryResponse = new RspOrderQueryEntity();
				queryResponse.setPayCode(RspOrderQueryEntity.PAY_CODE_WECHAT);
				if("SUCCESS".equals(response.getTrade_state())) {
					String time_end = response.getTime_end();
					LocalDateTime timeEnd = LocalDateTime.parse(time_end, DateUtil.yyyymmddhhmmss);
					Instant instant = timeEnd.atZone(ZoneId.systemDefault()).toInstant();
					int tradeEndTime = Math.toIntExact(instant.getEpochSecond());
					queryResponse.setStatus(1+"");
					queryResponse.setTradeEndTime(tradeEndTime);
					queryResponse.setTrade_no(response.getTransaction_id());
					queryResponse.setResult_msg("支付成功！");
					queryResponse.setResult_code("0000");
				}else {
					queryResponse.setStatus(0+"");
					queryResponse.setResult_msg(response.getTrade_state()+"_" + response.getTrade_state_desc());
					queryResponse.setResult_code(resultCode);
				}
				return ResultGenerator.genSuccessResult("success", queryResponse);
			}else {
				String msg = "err_code:" + response.getErr_code() + ",err_desc:"+response.getErr_code_des();
				return ResultGenerator.genFailResult(msg, null);
			}
		} catch (Exception e) {
			logger.error("微信app订单查询失败", e);
			e.printStackTrace();
		}
		return ResultGenerator.genFailResult("微信app支付查询失败", null);
	}
	private WxpayAppDTO wXpayAppModel2DTO(WxpayAppModel appModel) {
		if(null == appModel) {
			return null;
		}
		WxpayAppDTO dto = new WxpayAppDTO();
		dto.setAppid(appModel.getAppid());
		dto.setNoncestr(appModel.getNoncestr());
		dto.setPackageValue(appModel.getPackageValue());
		dto.setPartnerid(appModel.getPartnerid());
		dto.setPrepayid(appModel.getPrepayid());
		dto.setSign(appModel.getSign());
		dto.setTimestamp(appModel.getTimestamp());
		return dto;
	}
	/**
	 * 对请求进行签名
	 * @param param 要签名的参数
	 * @return
	 */
	private String sign(TreeMap<String, ?> param, String key){
		String paramUrl = this.joinKeyValue(new TreeMap<String, Object>(param),null,"&key="+key,"&",true,"sign_type","sign");
		logger.debug("微信待签名串:"+paramUrl);
		MessageDigest digestUtils = DigestUtils.getMd5Digest();
		try {
			digestUtils.update(paramUrl.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			digestUtils.update(paramUrl.getBytes());
		}
		byte[] sign = digestUtils.digest();
		String result = Hex.encodeHexString(sign);
		logger.debug("签名结果:"+result);
		return result;
	}
	/**
	 * 连接Map键值对
	 *
	 * @param map
	 *            Map
	 * @param prefix
	 *            前缀
	 * @param suffix
	 *            后缀
	 * @param separator
	 *            连接符
	 * @param ignoreEmptyValue
	 *            忽略空值
	 * @param ignoreKeys
	 *            忽略Key
	 * @return 字符串
	 */
	private String joinKeyValue(Map<String, Object> map, String prefix, String suffix, String separator,
			boolean ignoreEmptyValue, String... ignoreKeys) {
		List<String> list = new ArrayList<String>();
		if (map != null) {
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				String key = entry.getKey();
				String value = String.valueOf(entry.getValue());
				if (StringUtils.isNotEmpty(key) && !ArrayUtils.contains(ignoreKeys, key)
						&& (!ignoreEmptyValue || StringUtils.isNotEmpty(value))) {
					list.add(key + "=" + (value != null ? value : ""));
				}
			}
		}
		return (prefix != null ? prefix : "") + StringUtils.join(list, separator) + (suffix != null ? suffix : "");
	}
	/**
	 * 微信nonce_str生成算法
	 * @param bits 生成位数,选择64bit
	 * @return 生成后的nonce_str
	 */
	private String nonceStr(int bits) {
		final byte[] bytes;
		try {
			SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
			if ((bits % 8) != 0) {
				throw new IllegalArgumentException("Size is not divisible by 8!");
			}
			bytes = new byte[bits / 8];
			secureRandom.nextBytes(bytes);
			return Hex.encodeHexString(bytes);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return System.currentTimeMillis()+"";
	}
	/**
	 * Bean转map
	 * @param bean 要转的bean
	 * @return 返回一个TreeMap
	 */
	private TreeMap<String, String> bean2TreeMap(Object bean) {
		TreeMap<String, String> requestMap = new TreeMap<String, String>();
		Class<?> cls = bean.getClass();
		Field[] fields = cls.getDeclaredFields();
		try {
			for (int i = 0; i < fields.length; i++) {
				String key = fields[i].getName();
				fields[i].setAccessible(true);
				Object value = fields[i].get(bean);
				if ("sign".equals(key) || value == null || StringUtils.isEmpty(value.toString())) {
					continue;
				}
				if("packageValue".equals(key)) {
					requestMap.put("package", value.toString());
				}else {
					requestMap.put(key, value.toString());
				}
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return requestMap;
	}

}
