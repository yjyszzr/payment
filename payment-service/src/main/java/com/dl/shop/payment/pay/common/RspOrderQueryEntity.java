package com.dl.shop.payment.pay.common;

import java.io.Serializable;

import lombok.Data;
import net.sf.json.util.JSONUtils;

import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.dl.shop.payment.web.PaymentController;

@Data
public class RspOrderQueryEntity implements Serializable {
	private final static Logger logger = LoggerFactory.getLogger(RspOrderQueryEntity.class);
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
	public static final int TYPE_KUAIJIE_PAY = 4;// 天下银联扫码
	public static final int TYPE_UBEY = 5;// 天下银联扫码
	public static final int TYPE_LID = 6; //华移支付
	public static final int TYPE_APAY = 7;//艾支付
	public static final int TYPE_RKPAY = 8;//Q多多支付
	public static final int TYPE_JHPAY = 9;//Q多多支付
	public static final int TYPE_YUNPAY = 10;//云闪付
	
	public boolean isSucc() {
		if (!TextUtils.isEmpty(result_code) && type == TYPE_YINHE) {
			return result_code.equals("0000");
//		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_XIANFENG) {
//			return !StringUtils.isEmpty(trade_status) && "S".equalsIgnoreCase(trade_status);
//		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_YIFUTONG) {
//			return !StringUtils.isEmpty(result_code) && ("2".equalsIgnoreCase(result_code) || "3".equalsIgnoreCase(result_code));
//		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_TIANXIA_SCAN) {
//			return !StringUtils.isEmpty(result_code) && ("01".equalsIgnoreCase(result_code));// 成功
//		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_KUAIJIE_PAY) {
//			return !StringUtils.isEmpty(result_code) && ("Success".equalsIgnoreCase(result_code)||"RefundSuccess".equalsIgnoreCase(result_code));// 成功
//		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_UBEY) {
//			return !StringUtils.isEmpty(result_code) &&  "3".equalsIgnoreCase(result_code);// 成功
		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_LID) {
			return !StringUtils.isEmpty(result_code) &&  "1".equalsIgnoreCase(result_code);// 成功
		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_APAY) {
			return !StringUtils.isEmpty(result_code) &&  "1001".equalsIgnoreCase(result_code);// 成功
		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_RKPAY) {
			return !StringUtils.isEmpty(result_code) &&  "0".equalsIgnoreCase(result_code);// 成功
		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_JHPAY) {
			return !StringUtils.isEmpty(result_code) &&  "0".equalsIgnoreCase(result_code);// 成功
		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_YUNPAY) {
			return !StringUtils.isEmpty(result_code) &&  "1".equalsIgnoreCase(result_code);// 成功
		}
		
		return false;
	}

	public boolean isDoing() {
		if (!TextUtils.isEmpty(result_code) && type == TYPE_YINHE) {
			return result_code.equals("106");
//		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_XIANFENG) {
//			return StringUtils.isEmpty(trade_status) || "I".equalsIgnoreCase(trade_status);
//		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_YIFUTONG) {
//			return StringUtils.isEmpty(result_code) || "1".equalsIgnoreCase(result_code);
//		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_TIANXIA_SCAN) {
//			return StringUtils.isEmpty(result_code) || "00".equalsIgnoreCase(result_code);// 未支付
//		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_KUAIJIE_PAY) {
//			return !StringUtils.isEmpty(result_code) && ("Init".equalsIgnoreCase(result_code)||"Process".equalsIgnoreCase(result_code)
//					||"Wait".equalsIgnoreCase(result_code));// 成功
		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_LID) {
			return !StringUtils.isEmpty(result_code) &&  "0".equalsIgnoreCase(result_code);// 等待支付
		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_APAY) {
			return !StringUtils.isEmpty(result_code) &&  "1012".equalsIgnoreCase(result_code);// 等待支付
		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_RKPAY) {
			return !StringUtils.isEmpty(result_code) &&  "".equalsIgnoreCase(result_code);// 等待支付
		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_JHPAY) {
			return !StringUtils.isEmpty(result_code) &&  "".equalsIgnoreCase(result_code);// 等待支付
		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_YUNPAY) {
			return !StringUtils.isEmpty(result_code) &&  "".equalsIgnoreCase(result_code);// 等待支付
		}
		return false;
	}

	public boolean isFail() {
		if (!TextUtils.isEmpty(result_code) && type == TYPE_YINHE) {
			return Boolean.FALSE;
//			// return result_code.equals("104");
//		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_XIANFENG) {
//			return !StringUtils.isEmpty(trade_status) && "F".equalsIgnoreCase(trade_status);
//		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_YIFUTONG) {
//			return !StringUtils.isEmpty(result_code) && "0".equalsIgnoreCase(result_code);
//		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_TIANXIA_SCAN) {
//			return !StringUtils.isEmpty(result_code) && "02".equalsIgnoreCase(result_code);// 失败
//		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_KUAIJIE_PAY) {
//			return !StringUtils.isEmpty(result_code) && ("Fail".equalsIgnoreCase(result_code)||"RefundFail".equalsIgnoreCase(result_code));// 成功
//		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_UBEY) {
//			return !StringUtils.isEmpty(result_code) && !"2".equalsIgnoreCase(result_code);// 成功
		}else if (!TextUtils.isEmpty(result_code) && type == TYPE_LID) {
			return !StringUtils.isEmpty(result_code) &&  "2".equalsIgnoreCase(result_code);// 失败
		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_APAY) {
			return !StringUtils.isEmpty(result_code) &&  "1014".equalsIgnoreCase(result_code);// 失败
		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_RKPAY) {
			return !StringUtils.isEmpty(result_code) &&  !"0".equalsIgnoreCase(result_code);// 失败
		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_JHPAY) {
			return !StringUtils.isEmpty(result_code) &&  !"0".equalsIgnoreCase(result_code);// 失败
		} else if (!TextUtils.isEmpty(result_code) && type == TYPE_YUNPAY) {
			return !StringUtils.isEmpty(result_code) &&  "0".equalsIgnoreCase(result_code);// 失败
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
