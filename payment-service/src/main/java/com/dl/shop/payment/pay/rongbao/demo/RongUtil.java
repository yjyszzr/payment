package com.dl.shop.payment.pay.rongbao.demo;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.util.TextUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.shop.payment.pay.common.RspOrderQueryEntity;
import com.dl.shop.payment.pay.rongbao.config.ReapalH5Config;
import com.dl.shop.payment.pay.rongbao.entity.ReqRefundEntity;
import com.dl.shop.payment.pay.rongbao.entity.RspRefundEntity;
import com.dl.shop.payment.pay.rongbao.util.DecipherH5;
import com.dl.shop.payment.pay.rongbao.util.ReapalSubmit;

public class RongUtil {
	private final static Logger log = Logger.getLogger(RongUtil.class);
	
	/**
	 * 融宝订单回退
	 * @param reqEntity
	 * @return
	 * @throws Exception
	 */
	public static final RspRefundEntity refundOrderInfo(ReqRefundEntity reqEntity) throws Exception {
		RspRefundEntity rEntity = null;
		//原订单号
		String orig_order_no = reqEntity.getOrig_order_no();
		//退款金额
	    String amount = reqEntity.getAmount();
		//备注
		String note = reqEntity.getNote();
		Map<String, String> map = new HashMap<String, String>();
		map.put("merchant_id", ReapalH5Config.merchant_id);
		map.put("order_no", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
		map.put("orig_order_no", orig_order_no);
		BigDecimal total_fee = new BigDecimal(amount.toString()).movePointRight(2);
		map.put("amount", total_fee.toString());
		map.put("note", note);
		String url = "/fast/refund";
		String post;
		post = ReapalSubmit.buildSubmit(map, url);
		log.info("返回结果post==========>" + post);
		//解密返回的数据
		String res = DecipherH5.decryptData(post);
		if(!TextUtils.isEmpty(res)) {
			rEntity = JSON.parseObject(res,RspRefundEntity.class);
		}
		return rEntity;
	}
	
	public static final BaseResult<RspOrderQueryEntity> queryOrderInfo(String orderNo){
		RspOrderQueryEntity rEntity = null;
		Map<String, String> map = new HashMap<String, String>();
		map.put("merchant_id", ReapalH5Config.merchant_id);
		map.put("version", ReapalH5Config.version);
		map.put("order_no",orderNo);
		//请求接口/
		String url = "/fast/search";
		//返回结果
		String post;
		try {
			post = ReapalSubmit.buildSubmit(map, url);
			log.info("返回结果post==========>" + post);
		    //解密返回的数据
		    String res = DecipherH5.decryptData(post);
		    log.info("解密返回的数据==========>" + res);
		    if(!TextUtils.isEmpty(res)) {
		    	rEntity = JSON.parseObject(res,RspOrderQueryEntity.class);
		    	rEntity.setPayCode(RspOrderQueryEntity.PAY_CODE_RONGBAO);
		    	return ResultGenerator.genSuccessResult("succ",rEntity);
		    }else {
		    	return ResultGenerator.genFailResult("资金方回调失败~");
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResultGenerator.genFailResult("请求资金方回调失败~");
	}

	
	public static void main(String[] args) {
//		try {
//			BaseResult<RspOrderQueryEntity> rEntity = queryOrderInfo("20180503182485710280002");
//			log.info("rEntity:" + rEntity);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		ReqRefundEntity reqEntity = new ReqRefundEntity();
		reqEntity.setAmount("12");
		reqEntity.setNote("我要退款");
		reqEntity.setOrig_order_no("123456");
		try {
			RspRefundEntity rEntity = refundOrderInfo(reqEntity);
			System.out.println("rEntity:" + rEntity.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
