package com.dl.shop.payment.pay.rongbao.cash;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import com.alibaba.fastjson.JSON;
import com.dl.shop.payment.pay.rongbao.cash.cfg.CashConfig;
import com.dl.shop.payment.pay.rongbao.cash.entity.ReqCashEntity;
import com.dl.shop.payment.pay.rongbao.cash.model.AgentPayRequest;
import com.dl.shop.payment.pay.rongbao.cash.util.ReapalUtil;
import com.dl.shop.payment.pay.rongbao.util.HttpClientUtil;

/**
 * 融宝提现工具类
 */
public class CashUtil {
	@Resource
	private CashConfig cashCfg;
	
	/***
	 * 发起提现请求
	 * @throws Exception 
	 */
	public static final void sendGetCashInfo(ReqCashEntity reqEntity) throws Exception {
		String batch_no = reqEntity.getBatch_no();
        String batch_count = reqEntity.getBatch_count();
        String batch_amount = reqEntity.getBatch_amount();
        String pay_type = reqEntity.getPay_type();
        String content = reqEntity.getContent();
        
   		AgentPayRequest agentPayRequest = new AgentPayRequest();
   		Map<String, String> map = new HashMap<String, String>(0);
		map.put("charset", ReapalUtil.getCharset());
		map.put("trans_time",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		map.put("notify_url", ReapalUtil.getNotify_url());
		map.put("batch_no", batch_no);
		map.put("batch_count", batch_count);
		map.put("batch_amount", batch_amount);
		map.put("pay_type", pay_type);
		map.put("content", content);
		
		String mysign = ReapalUtil.BuildMysign(map, ReapalUtil.getKey());
		
		System.out.println("签名结果==========>" + mysign);
		map.put("sign", mysign);
		map.put("sign_type", ReapalUtil.getSign_type());

		String json = JSON.toJSONString(map);

		Map<String, String> maps = ReapalUtil.addkey(json);
		maps.put("merchant_id", ReapalUtil.getMerchant_id());
		maps.put("version", ReapalUtil.getVersion());
		System.out.println("maps==========>" + com.alibaba.fastjson.JSON.toJSONString(maps));
		String post = HttpClientUtil.post(ReapalUtil.getUrl() + "agentpay/pay", maps);
		String res = ReapalUtil.pubkey(post);
		System.out.println("result:" + res);
	}
	
	public static void main(String[] args) {
		ReqCashEntity reqEntity = new ReqCashEntity();
		reqEntity.setBatch_no("1234567");
		reqEntity.setBatch_count("1");
		reqEntity.setContent("3,62220215080205389633,jack-cooper,工商银行,分行,支行,私,10,CNY,北京,北京,18910116131,身份证,420321199202150718,0001,12306,hh");
		reqEntity.setBatch_amount("10");
		reqEntity.setPay_type("1");
		try {
			sendGetCashInfo(reqEntity);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public CashUtil() {
	}
}
