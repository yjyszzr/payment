package com.dl.shop.payment.pay.smkpay.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dl.shop.payment.pay.smkpay.util.AESUtil;
import com.dl.shop.payment.pay.smkpay.util.HttpUtil;
import com.dl.shop.payment.pay.smkpay.util.RSAUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Function: 支付接口-快捷支付
 **/
@Slf4j
@Service
public class SmkPay {
    private final static Logger logger = LoggerFactory.getLogger(SmkPay.class);

    /**
     * 银行卡签约
     *
     * @param requestMap
     * @return
     * @throws Exception
     */
    public Map<String, String> bqpSign(Map<String, String> requestMap) throws Exception {
        String reqSeq = requestMap.get("reqSeq");//setReqSeq();
        String randomKey = requestMap.get("randomKey");//setRandomKey();
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        Map<String, String> messageMap = new HashMap<String, String>();
        messageMap.put("version", "1.0.0");//版本号	String(7)	必输	目前版本号：1.0.0
        messageMap.put("transCode", "BQP0001");//交易代码	String(10)	必输	BQP0001
        messageMap.put("reqSeq", reqSeq);//请求流水	String(30)	必输	数字+字母（唯一）
        messageMap.put("randomKey", randomKey);//加密随机数	String(32)	必输	对称密钥串
        messageMap.put("merCode", requestMap.get("merCode"));//商户号	String(12)	必输	商户在市民卡签约的商户代码
        messageMap.put("chainNo", "004");//渠道号	String(4)	必输	默认004
        messageMap.put("merCustId", requestMap.get("merCustId"));//商户客户号	String(32)	必输	商户系统内部客户编号（唯一）
        messageMap.put("name", requestMap.get("name"));//银行卡户名	String(30)	必输
        messageMap.put("certType", requestMap.get("certType"));//证件类型	String(1)	必输	默认0-身份证
        messageMap.put("certNo", AESUtil.encryptToHex(requestMap.get("certNo"), randomKey));//证件号码	String(256)	必输	需加密
        messageMap.put("phone", AESUtil.encryptToHex(requestMap.get("phone"), randomKey));//手机号码	String(256)	必输	银行预留手机号，需加密
        messageMap.put("cardType", requestMap.get("cardType"));//银行卡类型	String(1)	必输	D：借记卡C：贷记卡
        messageMap.put("cardNo", AESUtil.encryptToHex(requestMap.get("cardNo"), randomKey));//银行卡号	String(256)	必输	需加密
        messageMap.put("validDate", "");//有效期	String(256)	非必输	MMYY，需加密
        messageMap.put("cvv2", "");//cvv2	String(256)	非必输	需加密
        messageMap.put("verCode", requestMap.get("verCode"));//手机验证码	String(6)	非必输	第一次交互非必输，应答报文中smsFlag为1时可输
        messageMap.put("token", requestMap.get("token"));//四要素令牌	String(32)	非必输	第一次交互非必输，应答报文中smsFlag为1时可输
        messageMap.put("phoneToken", requestMap.get("phoneToken"));//验证码令牌	String(32)	非必输	第一次交互非必输，应答报文中smsFlag为1时可输
        String messageMapStr = gson.toJson(messageMap);
        //请求开放平台数据
        Map<String, String> openMap = new HashMap<String, String>();
        openMap.put("reqSeq", reqSeq);
        openMap.put("appId", requestMap.get("appId"));
        openMap.put("method", "bqpSign");
        openMap.put("bizContent", messageMapStr);
        openMap.put("sign_param", "appId,method,bizContent");
        //加签
        openMap.put("sign", RSAUtil.rsaSignByCert(openMap, requestMap.get("certPath"), requestMap.get("certPwd")));
        String message = gson.toJson(openMap);
        String respStr = HttpUtil.postReq(requestMap.get("requestUrl"), message);
        JSONObject json = JSON.parseObject(respStr);
        //验签
        if ("true".equals(json.getString("success"))) {
            Map<String, String> checkMap = new HashMap<String, String>();
            checkMap.put("reqSeq", reqSeq);
            checkMap.put("sign_param", "success,value");
            checkMap.put("success", json.getString("success"));
            checkMap.put("value", json.getString("value"));
            checkMap.put("sign", json.getString("sign"));
            checkMap.put("publicKey", requestMap.get("vertifyPublicKey"));
            if (RSAUtil.rsaCheck(checkMap)) {
                Map<String, String> resultMap = (Map<String, String>) json.parse(json.getString("value"));
                return resultMap;
            }
        }
        return null;
    }

    /**
     * 银行卡解约
     *
     * @param requestMap
     * @return
     * @throws Exception
     */
    public Map<String, String> bqpUnSign(Map<String, String> requestMap) throws Exception {
        String reqSeq = requestMap.get("reqSeq");//setReqSeq();
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        Map<String, String> messageMap = new HashMap<String, String>();
        messageMap.put("version", "1.0.0");//版本号	String(7)	必输	目前版本号：1.0.0
        messageMap.put("transCode", "BQP0002");//交易代码	String(10)	必输	BQP0002
        messageMap.put("reqSeq", reqSeq);//请求流水	String(30)	必输	数字+字母（唯一）
        messageMap.put("merCode", requestMap.get("merCode"));//商户号	String(12)	必输	商户在市民卡签约的商户代码
        messageMap.put("chainNo", "004");//渠道号	String(4)	必输	默认004
        messageMap.put("merCustId", requestMap.get("merCustId"));//商户客户号	String(32)	必输	商户系统内部客户编号（唯一）
        messageMap.put("shortCardNo", requestMap.get("shortCardNo"));//短卡号	String(10)	必输	由卡号前六后四位组成

        String messageMapStr = gson.toJson(messageMap);
        //请求开放平台数据
        Map<String, String> openMap = new HashMap<String, String>();
        openMap.put("reqSeq", reqSeq);
        openMap.put("appId", requestMap.get("appId"));
        openMap.put("method", "bqpUnSign");
        openMap.put("bizContent", messageMapStr);
        openMap.put("sign_param", "appId,method,bizContent");
        //加签
        openMap.put("sign", RSAUtil.rsaSignByCert(openMap, requestMap.get("certPath"), requestMap.get("CertPwd")));
        String message = gson.toJson(openMap);
        String respStr = HttpUtil.postReq(requestMap.get("requestUrl"), message);
        JSONObject json = JSON.parseObject(respStr);
        //验签
        if ("true".equals(json.getString("success"))) {
            Map<String, String> checkMap = new HashMap<String, String>();
            checkMap.put("reqSeq", reqSeq);
            checkMap.put("sign_param", "success,value");
            checkMap.put("success", json.getString("success"));
            checkMap.put("value", json.getString("value"));
            checkMap.put("sign", json.getString("sign"));
            checkMap.put("publicKey", requestMap.get("vertifyPublicKey"));
            if (RSAUtil.rsaCheck(checkMap)) {
                Map<String, String> resultMap = (Map<String, String>) json.parse(json.getString("value"));
                return resultMap;
            }
        }
        return null;
    }

    /**
     * 银行卡签约并支付
     *
     * @param requestMap
     * @return
     * @throws Exception
     */
    public Map<String, String> bqpSignAndPay(Map<String, String> requestMap) throws Exception {
        String reqSeq = requestMap.get("reqSeq");//setReqSeq();
        String randomKey = requestMap.get("randomKey");//setRandomKey();
        String dateTime = requestMap.get("dateTime");//setRandomKey();
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        Map<String, String> messageMap = new HashMap<String, String>();
        messageMap.put("version", "1.0.0");//版本号	String(7)	必输	目前版本号：1.0.0
        messageMap.put("transCode", "BQP0004");//交易代码	String(10)	必输	BQP0004
        messageMap.put("reqSeq", reqSeq);//请求流水	String(30)	必输	数字+字母（唯一）
        messageMap.put("randomKey", randomKey);//加密随机数	String(32)	必输	对称密钥串
        messageMap.put("merCode", requestMap.get("merCode"));//商户号	String(12)	必输	商户在市民卡签约的商户代码
        messageMap.put("chainNo", "004");//渠道号	String(4)	必输	默认004
        messageMap.put("merCustId", requestMap.get("merCustId"));//商户客户号	String(32)	必输	商户系统内部客户编号（唯一）
        messageMap.put("name", requestMap.get("name"));//银行卡户名	String(30)	必输
        messageMap.put("certType", requestMap.get("certType"));//证件类型	String(1)	必输	默认 0-身份证
        messageMap.put("certNo", AESUtil.encryptToHex(requestMap.get("certNo"), randomKey));//证件号码	String(256)	必输	需加密
        messageMap.put("phone", AESUtil.encryptToHex(requestMap.get("phone"), randomKey));//手机号码	String(256)	必输	银行预留手机号，需加密
        messageMap.put("cardType", requestMap.get("cardType"));//银行卡类型	String(1)	必输	D：借记卡C：贷记卡
        messageMap.put("cardNo", AESUtil.encryptToHex(requestMap.get("cardNo"), randomKey));//银行卡号	String(256)	必输	需加密
        messageMap.put("validDate", "");//有效期	String(256)	非必输	MMYY，需加密
        messageMap.put("cvv2", "");//cvv2	String(256)	非必输	需加密
        messageMap.put("orderNo", requestMap.get("orderNo"));//订单号	String(32)	必输	商户订单号
        messageMap.put("dateTime", dateTime);//交易时间	String(17)	必输	YYYYMMDD HH:MM:SS
        messageMap.put("amount", requestMap.get("amount"));//交易金额	String(11)	必输	以元为单位，保留小数点后2位
        messageMap.put("goods", requestMap.get("goods"));//商品信息	String(30)	必输	商品信息描述
        messageMap.put("verCode", requestMap.get("verCode"));//手机验证码	String(6)	非必输	第一次交互非必输，应答报文中smsFlag为1时可输
        messageMap.put("token", requestMap.get("token"));//四要素令牌	String(32)	非必输	第一次交互非必输，应答报文中smsFlag为1时可输
        messageMap.put("phoneToken", requestMap.get("phoneToken"));//验证码令牌	String(32)	非必输	第一次交互非必输，应答报文中smsFlag为1时可输
        messageMap.put("accSplitData", "");//分账域	String	非必输
        messageMap.put("reserved", "");//reserved	保留域	String	非必输
        String messageMapStr = gson.toJson(messageMap);
        //请求开放平台数据
        Map<String, String> openMap = new HashMap<String, String>();
        openMap.put("reqSeq", reqSeq);
        openMap.put("appId", requestMap.get("appId"));
        openMap.put("method", "bqpSignAndPay");
        openMap.put("bizContent", messageMapStr);
        openMap.put("sign_param", "appId,method,bizContent");
        //加签
        openMap.put("sign", RSAUtil.rsaSignByCert(openMap, requestMap.get("certPath"), requestMap.get("certPwd")));
        String message = gson.toJson(openMap);
        String respStr = HttpUtil.postReq(requestMap.get("requestUrl"), message);
        logger.info("SMK签约并支付返回结果：" + respStr);
        JSONObject json = JSON.parseObject(respStr);
        //验签
        if ("true".equals(json.getString("success"))) {
            Map<String, String> checkMap = new HashMap<String, String>();
            checkMap.put("reqSeq", reqSeq);
            checkMap.put("sign_param", "success,value");
            checkMap.put("success", json.getString("success"));
            checkMap.put("value", json.getString("value"));
            checkMap.put("sign", json.getString("sign"));
            checkMap.put("publicKey", requestMap.get("vertifyPublicKey"));
            logger.info("SMK签约并支付返回结果：" + checkMap);
            if (RSAUtil.rsaCheck(checkMap)) {
                Map<String, String> resultMap = (Map<String, String>) json.parse(json.getString("value"));
                return resultMap;
            }
        }
        Map<String, String> resultMap = new HashMap<String, String>();
        resultMap.put("status", "55");
        resultMap.put("respCode", json.getString("respCode"));
        resultMap.put("respDesc", json.getString("respDesc"));
        return resultMap;
    }

    public static void main(String[] args) {
        String respStr = "{\"success\":true,\"respDesc\":null,\"respCode\":null,\"value\":\"{\\\"respSeq\\\":\\\"D20191010150639000003911008204\\\",\\\"serialNo\\\":\\\"1920191010150640000144470019\\\",\\\"smsFlag\\\":\\\"1\\\",\\\"token\\\":\\\"20191010150640000144470018\\\"}\",\"sign\":\"Nc09dg9qWNM8fn1eKRp6McG9D86jSP0RJmFewXV6Te188FDcaq+r9leShmsx6cLagBDJ61R1PNduOeq2Qs4QUXmImRtKl5qoR2PRx3/+S+VRRUgp2GuQttsra/Ayaa6I2BPFUFZBPH0DNAGqrs+6IyOUU8tCr+Zt/OILkvOnkpZ1oszoK/+YWoi8gqTsVC2wWw77fb8QBxaCLOpegmJD86HYT+Qm17vLdmXVW+tQ4L7a8eNc93Fr9U9+GW/z7pPloDfQtT6jn/dfo2MFYD7g1bwg9mRIDx7DyK9hPTlV32N8vSRvlyp6dI1N+JU+GYWD42yoHmKhemTJBfS6wr2ehg==\"}";
        JSONObject json = JSON.parseObject(respStr);
        Map<String, String> resultMap = (Map<String, String>) json.parse(json.getString("value"));
        System.out.println(resultMap);
    }

    /**
     * 支付
     *
     * @param requestMap
     * @return
     * @throws Exception
     */
    public Map<String, String> bqpPay(Map<String, String> requestMap) throws Exception {
        String reqSeq = requestMap.get("reqSeq");//setReqSeq();
        String dateTime = requestMap.get("dateTime");//setRandomKey();
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        Map<String, String> messageMap = new HashMap<String, String>();
        messageMap.put("version", "1.0.0");//版本号	String(7)	必输	目前版本号：1.0.0
        messageMap.put("transCode", "BQP0003");//交易代码	String(10)	必输	BQP0003
        messageMap.put("reqSeq", reqSeq);//请求流水	String(30)	必输	数字+字母（唯一）
        messageMap.put("merCode", requestMap.get("merCode"));//商户号	String(12)	必输	商户在市民卡签约的商户代码
        messageMap.put("chainNo", "004");//渠道号	String(4)	必输	默认004
        messageMap.put("merCustId", requestMap.get("merCustId"));//商户客户号	String(32)	必输	商户系统内部客户编号（唯一）
        messageMap.put("orderNo", requestMap.get("orderNo"));//订单号	String(32)	必输	商户订单号
        messageMap.put("shortCardNo", requestMap.get("shortCardNo"));//短卡号	String(10)	必输	由卡号前六后四位组成
        messageMap.put("dateTime", dateTime);//交易时间	String(17)	必输	YYYYMMDD HH:MM:SS
        messageMap.put("amount", requestMap.get("amount"));//交易金额	String(11)	必输	以元为单位，保留小数点后2位
        messageMap.put("goods", requestMap.get("goods"));//商品信息	String(30)	必输	商品信息描述
        messageMap.put("verCode", requestMap.get("verCode"));//手机验证码	String(6)	非必输	第一次交互非必输，应答报文中smsFlag为1时可输
        messageMap.put("phoneToken", requestMap.get("phoneToken"));//验证码令牌	String(32)	非必输	第一次交互非必输，应答报文中smsFlag为1时可输
        messageMap.put("accSplitData", "");//分账域	String	非必输	详见文档3.1.1
        messageMap.put("reserved", "");//保留域	String	非必输

        String messageMapStr = gson.toJson(messageMap);
        //请求开放平台数据
        Map<String, String> openMap = new HashMap<String, String>();
        openMap.put("reqSeq", reqSeq);
        openMap.put("appId", requestMap.get("appId"));
        openMap.put("method", "bqpPay");
        openMap.put("bizContent", messageMapStr);
        openMap.put("sign_param", "appId,method,bizContent");
        //加签
        openMap.put("sign", RSAUtil.rsaSignByCert(openMap, requestMap.get("certPath"), requestMap.get("certPwd")));
        String message = gson.toJson(openMap);
        logger.info("请求开放平台数据:" + requestMap);
        String respStr = HttpUtil.postReq(requestMap.get("requestUrl"), message);
        logger.info("SMK支付返回结果：" + respStr);
        JSONObject json = JSON.parseObject(respStr);
        //验签
        if ("true".equals(json.getString("success"))) {
            Map<String, String> checkMap = new HashMap<String, String>();
            checkMap.put("reqSeq", reqSeq);
            checkMap.put("sign_param", "success,value");
            checkMap.put("success", json.getString("success"));
            checkMap.put("value", json.getString("value"));
            checkMap.put("sign", json.getString("sign"));
            checkMap.put("publicKey", requestMap.get("vertifyPublicKey"));
            if (RSAUtil.rsaCheck(checkMap)) {
                Map<String, String> resultMap = (Map<String, String>) json.parse(json.getString("value"));
                return resultMap;
            }
        }
        Map<String, String> resultMap = new HashMap<String, String>();
        resultMap.put("status", "55");
        resultMap.put("respCode", json.getString("respCode"));
        resultMap.put("respDesc", json.getString("respDesc"));
        return resultMap;
    }


    /**
     * 支付结果查询
     *
     * @param requestMap
     * @return
     * @throws Exception
     */
    public Map<String, String> bqpPayQuery(Map<String, String> requestMap) throws Exception {
        String reqSeq = requestMap.get("reqSeq");//setReqSeq();
        String dateTime = requestMap.get("dateTime");//setReqSeq();
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        Map<String, String> messageMap = new HashMap<String, String>();
        messageMap.put("version", "1.0.0");//版本号	String(7)	必输	目前版本号：1.0.0
        messageMap.put("transCode", "BQP0007");//交易代码	String(10)	必输	BQP0007
        messageMap.put("reqSeq", reqSeq);//请求流水	String(30)	必输	数字+字母（唯一）
        messageMap.put("merCode", requestMap.get("merCode"));//商户号	String(12)	必输	商户在市民卡签约的商户代码
        messageMap.put("chainNo", "004");//渠道号	String(4)	必输	默认004
        messageMap.put("serialNo", "");//支付流水号	String(32)	非必输	支付平台返回的流水号
        messageMap.put("orderNo", requestMap.get("orderNo"));//交易订单号	String(30)	非必输	商户订单号
        messageMap.put("dateTime", dateTime);//交易时间	String(17)	必输	YYYYMMDD HH:MM:SS

        String messageMapStr = gson.toJson(messageMap);
        //请求开放平台数据
        Map<String, String> openMap = new HashMap<String, String>();
        openMap.put("reqSeq", reqSeq);
        openMap.put("appId", requestMap.get("appId"));
        openMap.put("method", "bqpPayQuery");
        openMap.put("bizContent", messageMapStr);
        openMap.put("sign_param", "appId,method,bizContent");
        //加签
        openMap.put("sign", RSAUtil.rsaSignByCert(openMap, requestMap.get("certPath"), requestMap.get("certPwd")));
        String message = gson.toJson(openMap);
        String respStr = HttpUtil.postReq(requestMap.get("requestUrl"), message);
        JSONObject json = JSON.parseObject(respStr);
        //验签
        if ("true".equals(json.getString("success"))) {
            Map<String, String> checkMap = new HashMap<String, String>();
            checkMap.put("reqSeq", reqSeq);
            checkMap.put("sign_param", "success,value");
            checkMap.put("success", json.getString("success"));
            checkMap.put("value", json.getString("value"));
            checkMap.put("sign", json.getString("sign"));
            checkMap.put("publicKey", requestMap.get("vertifyPublicKey"));
            if (RSAUtil.rsaCheck(checkMap)) {
                Map<String, String> resultMap = (Map<String, String>) json.parse(json.getString("value"));
                return resultMap;
            }
        }
        return null;
    }
}
