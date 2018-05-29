package com.dl.shop.payment.pay.xianfeng.cash.entity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.springframework.stereotype.Component;
import org.springframework.test.annotation.Commit;

import com.dl.shop.payment.pay.xianfeng.cash.config.Constants;
import com.ucf.sdk.CoderException;
import com.ucf.sdk.UcfForOnline;
import com.ucf.sdk.util.UnRepeatCodeGenerator;

import lombok.Data;

@Component
@Data
public class ReqQueryCashEntity {
	private String service;
	private String secId;
	private String version;
	private String reqSn;
	private String merchantId;//merchantId
	private String merchantNo;
	private String sign;
	
	public final ReqQueryCashEntity buildReqQueryEntity(String orderNo,String secId,String version,String merchantID,String MER_RSAKEY) throws CoderException, GeneralSecurityException {
		ReqQueryCashEntity reqEntity = new ReqQueryCashEntity();
		reqEntity.service = "REQ_WITHDRAW_QUERY_BY_ID";
		reqEntity.secId = secId;
		reqEntity.version = version;		
		reqEntity.merchantId = merchantID;
		reqEntity.merchantNo = orderNo;
		reqEntity.reqSn = UnRepeatCodeGenerator.createUnRepeatCode(reqEntity.merchantId, reqEntity.service, new SimpleDateFormat("yyyyMMddhhmmssSSS").format(new Date()));;
		HashMap<String,String> mMap = new HashMap<>();
		mMap.put("service",reqEntity.service);
		mMap.put("secId",reqEntity.secId);
		mMap.put("version",reqEntity.version);
		mMap.put("reqSn",reqEntity.reqSn);
		mMap.put("merchantId",reqEntity.merchantId);
		mMap.put("merchantNo",reqEntity.merchantNo);
		String signValue = UcfForOnline.createSign(MER_RSAKEY, "sign", mMap, "RSA");
		reqEntity.sign = signValue;
		return reqEntity;
	}
	
	
	public String buildReqStr() throws UnsupportedEncodingException {
		StringBuilder builder = new StringBuilder();
		builder.append("service="+service);
		builder.append("&");
		builder.append("secId="+secId);
		builder.append("&");
		builder.append("version="+version);
		builder.append("&");
		builder.append("reqSn="+reqSn);
		builder.append("&");
		builder.append("merchantId="+merchantId);
		builder.append("&");
		builder.append("merchantNo="+merchantNo);
		builder.append("&");
		builder.append("sign="+URLEncoder.encode(sign,"UTF-8"));
		return builder.toString();
	} 
}
