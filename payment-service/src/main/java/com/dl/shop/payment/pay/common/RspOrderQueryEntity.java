package com.dl.shop.payment.pay.common;

import java.io.Serializable;

import lombok.Data;

import org.apache.http.util.TextUtils;
import org.springframework.util.StringUtils;

@Data
public class RspOrderQueryEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	private String merchant_id;
	private String order_no;
	private String result_code;
	private String result_msg;
	private String status;
	private String timestamp;
	private String total_fee;
	private String trade_no;
	private String trade_status;

	// 微信支付使用
	private Integer tradeEndTime;
	private String payCode;

	private int type;
	public static final int TYPE_YINHE = 0;
	public static final int TYPE_XIANFENG = 1;
	public static final int TYPE_YIFUTONG = 2;
	public static final int TYPE_TIANXIA_SCAN = 3;// 天下银联扫码

	public boolean isSucc() {
		if (!TextUtils.isEmpty(result_code) && type == TYPE_YINHE) {
			return result_code.equals("0000");
		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_XIANFENG) {
			return !StringUtils.isEmpty(trade_status) && "S".equalsIgnoreCase(trade_status);
		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_YIFUTONG) {
			return !StringUtils.isEmpty(result_code) && ("2".equalsIgnoreCase(result_code) || "3".equalsIgnoreCase(result_code));
		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_TIANXIA_SCAN) {
			return !StringUtils.isEmpty(result_code) && ("01".equalsIgnoreCase(result_code));// 成功
		}
		return false;
	}

	public boolean isDoing() {
		if (!TextUtils.isEmpty(result_code) && type == TYPE_YINHE) {
			return result_code.equals("106");
		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_XIANFENG) {
			return StringUtils.isEmpty(trade_status) || "I".equalsIgnoreCase(trade_status);
		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_YIFUTONG) {
			return StringUtils.isEmpty(result_code) || "1".equalsIgnoreCase(result_code);
		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_TIANXIA_SCAN) {
			return StringUtils.isEmpty(result_code) || "00".equalsIgnoreCase(result_code);// 未支付
		}
		return false;
	}

	public boolean isFail() {
		if (!TextUtils.isEmpty(result_code) && type == TYPE_YINHE) {
			return Boolean.FALSE;
			// return result_code.equals("104");
		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_XIANFENG) {
			return !StringUtils.isEmpty(trade_status) && "F".equalsIgnoreCase(trade_status);
		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_YIFUTONG) {
			return !StringUtils.isEmpty(result_code) && "0".equalsIgnoreCase(result_code);
		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_TIANXIA_SCAN) {
			return !StringUtils.isEmpty(result_code) && "02".equalsIgnoreCase(result_code);// 失败
		}
		return false;
	}

	// 104 -> 未支付
	// 404 -> 订单不存在
	public boolean isYinHeWeChatNotPay() {
		return "104".equals(result_code) || "404".equals(result_code);
	}

	public static final String PAY_CODE_WECHAT = "app_weixin";
	public static final String PAY_CODE_RONGBAO = "app_rongbao";
	public static final String PAY_CODE_XIANFENG = "app_xianfeng";
}
