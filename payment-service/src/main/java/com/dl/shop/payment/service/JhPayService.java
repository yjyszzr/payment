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
import java.util.Random;
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
	private String privateKey = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCBoB8DaBh3n2ORIpHz9IZaGca5ODkf8KOZ4yIAGdoqtuJ3DKcO0U4cCQ4i3WA/NbY2jRFsdugAGAB31jxBJU0hW9/8/nkprTkBUjGLvo0qM/6QA3LAz6ftMb4Wy4n9IJdG7flWp/dfq43mBMhMGFQnzdQsTAGNf0thIT3r7vni4VaV+6d498hKomHEmaGd5entMbWMzUxmUBRATrI0D19q4Pmb3s0zZ1KAPvsgP3dX4Ob7vGtPDIS1gnfcVF+X5k9Qh+AiRcItkufovdNZKIhpOOSegw69tlkN0+IC5s+vKgdf/Df653UyOkbowEAiXHCbHdhZeBl3JnlzRvsGP+o5AgMBAAECggEALSpQhAxaMhwMQj6sYjc6NNy0XEcVyGY9ato0N5rKZIWypM54yvXs5GHXVhiSd5PMeHdwsb1Amv/B8f9ooT5RAKWVhv5X00izCx3Le6iaYTEaqq0456uzFDd3x1OGMfeQHju+3A3MaL5Kw/G4pKL5d6EFI6YcCGRpLF+7xqBO21ksWH1w/YbdtwupHimFw3b1yECC/NgR6BWOpTM/5BAHsnjk270JaOD5HtwKbJIKmVT4Wp9ix/0MTMu9l6n6uGMgSi1V4S1F7/+XOsdPAmboTAYZFc1T6MuqbaAKRMeJt9XPKJHiGpw8/QfKcWrlGq0P5sGuKkUnfqakVcHrNJr1AQKBgQDlIf4lpJ7bRLeyHIwoYthXmW+ANUFbb8j4M5d2g+u1UXOgt4YRtW5t6FWCux6dIOnuNjaqtiGxR+uP/ZSOHSS1Hp2RPKCwXtZLDiwee2Fjt1LrFUmA2gxwbeZWM906M1vJ3kac6sUMAWd5ne65qiC6tT2sUoEfU49B5G8PMgRlmQKBgQCQ0ylhjcPxKTFvohqL10HM21EqpJCKXuVLh8dO+nq7DkQFMiD06kDvFrGdVtRKog5Cn0Bi3s7F6NdaRbyvpvk+1ZNbKs2KG7dAWU1J/Ddg7n/0Zw+KCAlYH+rEW7xkPcf+8iGl/9GbhkOOMfit0Dm1YmCA6pkCo5hCgar3UHVNoQKBgFydgtbAVZ2XhxCtTVG9smimElWmMQa+hmMcp2o2JH4jsDMUO1LJHRu7v2SaMeOdPDEXJL2X5MJ9qY+IFhXjXcT/3PypnuHrU37++YJQqKrfnNp8vjsg58pCAcpyKEewHrfX6n7evkr9/k9AMRBG3ffZ7lXK+3ooEk22AdYIh5JxAoGAaB64WH+AiMhR121W4oTutKZU03Cezixtc4D5pOlmBUe7VXT1xr8H0hyhs3myhLm/wNwXgT2osRa2hRswDaTg+vC30UqTnSBR/jx12aQv+EtfyMmznUwr06SWt3cwmWzldYCE+oBSJRtBRGDe5a+XXbWNpgGD4ibVl7L7xHSe8SECgYB8tS8TZG65XRo3UpYHiPf+wAVCfwYeBxUjCoKptXZZ/q5Pkx+bY8Ifxs8DEDtToFvo/1dc7QFkhm95rv1CKyNbLgMf4K1PJTyFFLOJSk2ZNs7RFjP9A4YNqM68fcsne2HRFWxcvyw3g0dcun2iV2hy51H1LOPGpVh3gEHzhuK1RA==";
	private String publicKey="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC2Kkh9RtTow6vZmarWD96snudqD69brU339nbrTIiv6tR61s7n1vTSLLDkZ6a21DC8P6xZHPkI2caa5LOudGWvBPz1vQN/+amw0V3ZfSZWWFbFzfHqndaeYyC+xEAdIW9rEOcgvkiZjQn2OvNhYrAShBDLFLUb+G9ZkQ3+N3Vu7QIDAQAB";
	/**
	 * 发送消息体到服务端
	 * 
	 * @param params
	 * @return
	 */
	@SuppressWarnings("finally")
	public String jhAliPay(SortedMap<String, String> map){
		String key = zfbutil.getSECRET_C();
		String mch_id = zfbutil.getMERCHANT_NO_C();
		int rnumber = new Random().nextInt(4)+1;
		if(rnumber%4==0) {
			key = zfbutil.getSECRET_C();
			mch_id = zfbutil.getMERCHANT_NO_C();
		}else if(rnumber%3==0) {
			key = zfbutil.getSECRET_D();
			mch_id = zfbutil.getMERCHANT_NO_D();
		}else if(rnumber%2==0) {
			key = zfbutil.getSECRET_D();
			mch_id = zfbutil.getMERCHANT_NO_D();
		}
		
		map.put("service", zfbutil.getPAY_URL());
		map.put("mch_id", mch_id);
		map.put("mch_create_ip", "127.0.0.1");
		map.put("notify_url", zfbutil.getNOTIFY_URL());
		map.put("nonce_str", String.valueOf(new Date().getTime()));
		
		Map<String, String> params = SignUtils.paraFilter(map);
		StringBuilder buf = new StringBuilder((params.size() + 1) * 10);
		SignUtils.buildPayParams(buf, params, false);
		String preStr = buf.toString();
		String sign = MD5.sign(preStr, "&key=" + key, "utf-8");
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
					if (!SignUtils.checkParam(resultMap, key)) {
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
//		param.put("total_fee", "1");//测试1分
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
//        req.setMchNo(zfbutil.getMERCHANT_NO());
//        req.setAuthMchNo(zfbutil.getMERCHANT_NO());
        req.setMchNo("100530000003");
        req.setAuthMchNo("100530000003");
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
     * 代付
     *
     * @throws JsonProcessingException
     * @throws InterruptedException 
     */
    public SftPayPayResponse testPayFor() throws JsonProcessingException, UnsupportedEncodingException, InterruptedException {
        SftPayPayRequest req = new SftPayPayRequest();
        String key = UUID.randomUUID().toString();
        // 生成支付者信息
        Map<String, String> payerInfoMap = new HashMap<String, String>();
        payerInfoMap.put("id_no", "32058219870706111X");
        payerInfoMap.put("phone", "13500000000");
        payerInfoMap.put("name", "全渠道");
        payerInfoMap.put("account_no","6216261000000000018");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        String payerInfo = objectMapper.writeValueAsString(payerInfoMap);
        req.setOutTradeNo("123123123123");
        req.setTotalFee(300L);
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
        return payfor(req,0);
    }

    /**
     * 统一订单查询
     * @throws InterruptedException 
     *
     * @throws JsonProcessingException
     */
    public SftPayQueryOrderResponse testQueryOrder() throws InterruptedException {
        SftPayQueryOrderRequest req = new SftPayQueryOrderRequest();
        req.setOutTradeNo("123123123123");
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
		if(paid>5000) {
			return true;
		}
		return false;
	}
	public static void main(String[] args) {
		for (int i = 0; i < 100; i++) {
//			int rnumber = new Random().nextInt(4)+1;
//			System.out.println(rnumber);
			int _4 = 0,_3=0,_2=0,_1=0;
			for (int j = 0; j < 100; j++) {
				int rnumber = new Random().nextInt(4)+1;
				if(rnumber%4==0) {
					_4++;
				}else if(rnumber%3==0) {
					_3++;
				}else if(rnumber%2==0) {
					_2++;
				}else{
					_1++;
				}
			}
			
			System.out.println("随机数生成次数："+_4+"_"+_3+"_"+_2+"_"+_1);

		}
	}
}
