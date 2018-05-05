package com.dl.shop.payment.pay.rongbao.demo;

import java.util.HashMap;
import java.util.Map;
import org.apache.http.util.TextUtils;
import com.alibaba.fastjson.JSON;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.shop.payment.pay.common.RspOrderQueryEntity;
import com.dl.shop.payment.pay.rongbao.config.ReapalH5Config;
import com.dl.shop.payment.pay.rongbao.util.DecipherH5;
import com.dl.shop.payment.pay.rongbao.util.ReapalSubmit;

public class RongUtil {
	
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
			System.out.println("返回结果post==========>" + post);
		    //解密返回的数据
		    String res = DecipherH5.decryptData(post);
		    System.out.println("解密返回的数据==========>" + res);
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
		try {
			BaseResult<RspOrderQueryEntity> rEntity = queryOrderInfo("20180503182485710280002");
			System.out.println("rEntity:" + rEntity);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
