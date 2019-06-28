package com.dl.shop.payment.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.pay.jhpay.JhPayZFBUtils;
import com.dl.shop.payment.pay.jhpay.model.DrLimitAmountRequest;
import com.dl.shop.payment.pay.jhpay.model.DrLimitAmountResponse;
import com.dl.shop.payment.pay.jhpay.model.SftPayBaseRequest;
import com.dl.shop.payment.pay.jhpay.model.SftPayBaseResponse;
import com.dl.shop.payment.pay.jhpay.model.SftPayPayRequest;
import com.dl.shop.payment.pay.jhpay.model.SftPayPayResponse;
import com.dl.shop.payment.pay.jhpay.model.SftPayQueryOrderRequest;
import com.dl.shop.payment.pay.jhpay.model.SftPayQueryOrderResponse;
import com.dl.shop.payment.pay.jhpay.util.CharsetUtil;
import com.dl.shop.payment.pay.jhpay.util.JsonUtil;
import com.dl.shop.payment.pay.jhpay.util.MD5;
import com.dl.shop.payment.pay.jhpay.util.RSAUtil;
import com.dl.shop.payment.pay.jhpay.util.SftSignUtil;
import com.dl.shop.payment.pay.jhpay.util.SignUtils;
import com.dl.shop.payment.pay.jhpay.util.TripleDesUtil;
import com.dl.shop.payment.pay.jhpay.util.XmlUtils;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.entity.TXScanRequestPaidByOthers;
import com.dl.shop.payment.pay.xianfeng.cash.entity.RspSingleCashEntity;
import com.dl.shop.payment.web.PaymentController;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JhPayService {
	private final static Logger logger = LoggerFactory.getLogger(PaymentController.class);
	@Resource
	private JhPayZFBUtils zfbutil;
	private String baseUrl="http://pcrapi.test.swiftpass.cn/auth";
	private String privateKey = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDokHNqfrAPfa+7\r\n" + 
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
			"W0Y7vFedTfKaUdR34DzWKTFcVw==";
	private String publicKey="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhz/sY09mWmmx8fA48MArLgO6fMQNWriqSS6fUPKP/ysWoa0MgJGdusRGOxldGYQlV2bPGiE74wcrV1b0VH6YdjETIkfTD5UwN/v+2G3gAfQffsy3EZ5U5oCNR7n5fBdIvBYqQ4js4bB5BERCpbhpqqqfw8fNcolWL5dPPlX9rVBpqKYBjp18e66v03hVLp7q9NIpGNZSJMgkMp9pIgKsyH1W928k/fn6RTP1VHVzeHl9/lJwturo66KyN98iCGsLSpMlZa6vRFescLHjrz/Nf29TI0VRmIlMrNsBG4Ic2AYXMJ5IjT7apb9XGQALgEEK41z9LKvcq7f9IyNb4us3cQIDAQAB";
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
	
	
	
	
	
	private SftPayBaseResponse call(SftPayBaseRequest req, String method, Class<? extends SftPayBaseResponse> clazz) {
        req.setMchNo(zfbutil.getMERCHANT_NO());
        req.setAuthMchNo(zfbutil.getMERCHANT_NO());
        req.setCharset("UTF-8");
        req.setMchKeyVer(1);
        req.setPlatKeyVer(1);
        req.setVersion("1.0");
        req.setSignType("SHA256WithRSA");
        req.setNonceStr(UUID.randomUUID().toString());
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime now = LocalDateTime.now();
        String formatDateTime = now.format(formatter);
        req.setReqTime(formatDateTime);

        ObjectMapper xmlMapper = new ObjectMapper();

        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        Map<String, String> reqDataMap = xmlMapper.convertValue(req, new TypeReference<Map<String, String>>() {
        });
        String sign = SftSignUtil.sign(reqDataMap, privateKey);
        reqDataMap.put("sign", sign);

        String reqBody;
        try {
            reqBody = xmlMapper.writer().writeValueAsString(reqDataMap);
            log.info("请求报文：{}", reqBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("SERIALIZED_ERROR", e);
        }
        HttpPost httpReq = new HttpPost(baseUrl + "/" + method);
        httpReq.setEntity(new StringEntity(reqBody, ContentType.create("text/json", CharsetUtil.UTF_8)));
        CloseableHttpResponse httpResponse = null;
        String respBody = null;
        try {
            httpResponse = HttpClients.createDefault().execute(httpReq);
            respBody = EntityUtils.toString(httpResponse.getEntity(), CharsetUtil.UTF_8);
            log.info("返回报文：{}", respBody);
        } catch (IOException e) {
            throw new RuntimeException("NETWORK_ERROR", e);
        } finally {
            if (httpResponse != null) {
                EntityUtils.consumeQuietly(httpResponse.getEntity());
            }
        }
        Map<String, String> respDataMap = null;
        try {
            respDataMap = xmlMapper.readValue(respBody.getBytes(CharsetUtil.UTF_8),
                    new TypeReference<Map<String, String>>() {
                    });
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("WRONG_RESPONSE_FORMAT", e);
        }

        SftSignUtil.verifySign(respDataMap, publicKey);

        return xmlMapper.convertValue(respDataMap, clazz);
    }
    public SftPayQueryOrderResponse queryOrder(SftPayQueryOrderRequest req,int count) throws InterruptedException {
    	SftPayQueryOrderResponse sftPayQuery = (SftPayQueryOrderResponse) call(req, "queryorder", SftPayQueryOrderResponse.class);
    	if("0".equals(sftPayQuery.getStatus())) {//代付接收成功
    		if(!"2".equals(sftPayQuery.getTradeStateStr())) {//代付未成功
    			if("0".equals(sftPayQuery.getNeedQuery()) || "3".equals(sftPayQuery.getTradeStateStr())) {//不需要重新查询 或者 代付失败
            		//不需要重新代付
            	}else {//需要重新代付
            		if(count>3) {
            			return sftPayQuery;
            		}
            		Thread.sleep(3000);
            		queryOrder(req,count++);
            	}
    		}
        }
        return sftPayQuery;
    }

    public SftPayPayResponse payfor(SftPayPayRequest req,int count) throws InterruptedException {
    	SftPayPayResponse sftPayPay = (SftPayPayResponse) call(req, "payfor", SftPayPayResponse.class); //代付完成
    	if("0".equals(sftPayPay.getStatus())) {//代付接收成功
    		if(!"2".equals(sftPayPay.getTradeState())) {//代付未成功
    			if("0".equals(sftPayPay.getNeedQuery()) || "3".equals(sftPayPay.getTradeState())) {//不需要重新查询 或者 代付失败
            		//不需要重新代付
            	}else {//需要重新代付
            		if(count>3) {
            			return sftPayPay;
            		}
            		Thread.sleep(3000);
            		payfor(req,count++);
            	}
    		}
        }
        return sftPayPay;

    }

    public DrLimitAmountResponse queryLimitAmount(DrLimitAmountRequest request,int count) throws InterruptedException {
    	DrLimitAmountResponse dresult = (DrLimitAmountResponse) call(request, "queryLimitAmount", DrLimitAmountResponse.class);
    	if("0".equals(dresult.getStatus())) {//账号余额查询接收成功
			if("0".equals(dresult.getNeedQuery())) {//不需要重新查询 或者 代付失败
        		//不需要重新代付
        	}else {//需要重新代付
        		if(count>3) {
        			return dresult;
        		}
        		Thread.sleep(3000);
        		queryLimitAmount(request,count++);
        	}
        }
    	
    	
        return dresult;

    }
	
    /**
              * 代付
     *
     * @throws Exception
     */
    public RspSingleCashEntity fundApply(TXScanRequestPaidByOthers txScanRequestPaidByOthers) throws Exception {
    	String amount = txScanRequestPaidByOthers.getTxnAmt()!=null?txScanRequestPaidByOthers.getTxnAmt():"0";
    	RspSingleCashEntity rspEntity = new RspSingleCashEntity();
        SftPayPayRequest req = new SftPayPayRequest();
        String key = UUID.randomUUID().toString();
        // 生成支付者信息
        Map<String, String> payerInfoMap = new HashMap<String, String>();
        payerInfoMap.put("id_no", txScanRequestPaidByOthers.getCertNum());
        payerInfoMap.put("phone", txScanRequestPaidByOthers.getMobile());
        payerInfoMap.put("name", "全渠道");
        payerInfoMap.put("account_no",txScanRequestPaidByOthers.getAccountNo());
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        String payerInfo = objectMapper.writeValueAsString(payerInfoMap);
        req.setOutTradeNo(txScanRequestPaidByOthers.getOrderId());
        req.setTotalFee(Long.valueOf(amount));
        // 加密支付者信息
        req.setPayerInfo(new String(
                Base64.getEncoder()
                        .encode(TripleDesUtil.encrypt(TripleDesUtil.CipherSuite.ECB_PKCS5PADDING,
                                payerInfo.getBytes(CharsetUtil.UTF_8), key.getBytes(CharsetUtil.UTF_8))),
                CharsetUtil.UTF_8));
        // 加密对称密钥
        req.setEncryptedKey(new String(
                Base64.getEncoder()
                        .encode(RSAUtil.encrypt(RSAUtil.CipherSuite.ECB_PKCS1PADDING, key.getBytes(CharsetUtil.UTF_8),
                        		publicKey)),
                CharsetUtil.UTF_8));
        SftPayPayResponse sftPayPay = payfor(req,0); //代付完成
    	if("0".equals(sftPayPay.getStatus())) {//代付接收成功
    		if("2".equals(sftPayPay.getTradeState())) {//代付成功
    			rspEntity.status = "S";
    			rspEntity.resMessage = "提现成功";
    		} else if("3".equals(sftPayPay.getTradeState())) {//代付失败
    			rspEntity.status = "F";
    			rspEntity.resMessage = sftPayPay.getErrMsg();
    		} else {//代付处理中  
    			SftPayQueryOrderRequest reqs = new SftPayQueryOrderRequest();
    			reqs.setOutTradeNo(txScanRequestPaidByOthers.getOrderId());
    	        reqs.setBankcardNo("6225880171378335");
    	        SftPayQueryOrderResponse  sftPayQuery = queryOrder(reqs,0);//查询代付结果
    	        if("0".equals(sftPayQuery.getStatus())) {//查询代付接收成功
    	    		if("2".equals(sftPayQuery.getTradeStateStr())) {//代付成功
    	    			rspEntity.status = "S";
    	    			rspEntity.resMessage = "提现成功";
    	    		} else if("3".equals(sftPayQuery.getTradeStateStr())) {//代付失败
    	    			rspEntity.status = "F";
    	    			rspEntity.resMessage = sftPayPay.getErrMsg();
    	    		} else {//代付处理中 （避免损失默认成功，如果失败执行人工退款）
    	    			rspEntity.status = "S";
    	    			rspEntity.resMessage = sftPayPay.getMessage();
    	    		}
    	    	}else {//代付查询接收失败
    	    		rspEntity.status = "S";
    	    		rspEntity.resMessage = sftPayPay.getMessage();
    	    	}
    		}
    	}else {//代付接收失败（避免损失默认成功，如果失败执行人工退款）
    		rspEntity.status = "S";
    		rspEntity.resMessage = sftPayPay.getMessage();
    	}
        return rspEntity;
    }


    /**
     * 统一订单查询
     * @throws InterruptedException 
     *
     * @throws JsonProcessingException
     */
    public SftPayQueryOrderResponse testQueryOrder() throws InterruptedException {
        SftPayQueryOrderRequest req = new SftPayQueryOrderRequest();
        req.setOutTradeNo("1548296247390");
        req.setBankcardNo("6225880171378335");
        return queryOrder(req,0);
    }

    public DrLimitAmountResponse testQueryLimitAmount() throws InterruptedException{
        DrLimitAmountRequest request=new DrLimitAmountRequest();
        return queryLimitAmount(request,0);
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
		if(paid>3000) {
			return true;
		}
		return false;
	}
}
