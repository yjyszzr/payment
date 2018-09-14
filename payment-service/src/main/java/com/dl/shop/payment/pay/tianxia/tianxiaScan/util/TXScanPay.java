package com.dl.shop.payment.pay.tianxia.tianxiaScan.util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dl.shop.payment.pay.tianxia.tianxiaScan.config.TXPayConfig;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.entity.TXScanRequestPay;

public class TXScanPay {
	private final static Logger logger = LoggerFactory.getLogger(TXScanPay.class);

	/**
	 * 下单接口
	 */
	public static String txScanPay(TXScanRequestPay txScanRequestPay) {

		HttpApi http = new HttpApi(TXPayConfig.REQ_URL, HttpApi.POST);
		Map<String, Object> data = new HashMap<String, Object>();
		Map<String, Object> hdata = new HashMap<String, Object>();
		data.put("agtId", txScanRequestPay.getAgtId());
		data.put("merId", txScanRequestPay.getMerId());
		data.put("tranCode", "1101");

		data.put("nonceStr", TdExpBasicFunctions.RANDOM(16, "0"));
		data.put("orderAmt", txScanRequestPay.getOrderAmt());
		String orderId = TdExpBasicFunctions.RANDOM(19, "2");
		logger.info("订单编号:" + orderId);
		data.put("orderId", txScanRequestPay.getOrderId());
		data.put("notifyUrl", txScanRequestPay.getNotifyUrl());

		data.put("goodsName", TdExpBasicFunctions.STR2HEX("驱虫药"));

		// 明细中单价为分
		String detail = txScanRequestPay.getGoodsDetail();
		data.put("goodsDetail", TdExpBasicFunctions.STR2HEX(detail));
		data.put("stlType", "T0");
		data.put("payChannel", "WXPAY");
		String sign = HttpApi.getSign(data, TXPayConfig.MD5KEY);
		logger.info(sign);
		try {
			sign = SecurityUtil.sign(sign, TXPayConfig.PRVKEY, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		hdata.put("sign", sign);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("REQ_BODY", data);
		map.put("REQ_HEAD", hdata);
		String string = JUtil.toJsonString(map);
		logger.info("toJsonString:" + string);
		String rdata = http.post(string);
		logger.info(":" + rdata);
		Map<String, Object> rmap = JUtil.toMap(rdata);
		logger.info(" JUtil.toMap:", rmap);
		@SuppressWarnings("unchecked")
		Map<String, Object> _body = (Map<String, Object>) rmap.get("REP_BODY");
		@SuppressWarnings("unchecked")
		Map<String, Object> _head = (Map<String, Object>) rmap.get("REP_HEAD");
		String vsign = HttpApi.getSign(_body, TXPayConfig.MD5KEY);
		logger.info("获取签名:" + vsign);
		String _sign = _head.get("sign").toString();
		try {
			boolean flag = SecurityUtil.verify(vsign, _sign, TXPayConfig.PUBKEY, true);
			logger.info("验证签名状态:" + flag);
			if (flag) {
				if (_body.containsKey("codeUrl")) {
					return _body.get("codeUrl").toString();
				}
				logger.info(TdExpBasicFunctions.HEX2STR(_body.get("rspmsg").toString()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return orderId;
	}
}
