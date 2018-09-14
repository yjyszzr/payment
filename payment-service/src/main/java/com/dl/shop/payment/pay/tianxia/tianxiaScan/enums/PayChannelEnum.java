package com.dl.shop.payment.pay.tianxia.tianxiaScan.enums;

public enum PayChannelEnum {

	WXPAY(1, "微信支付"), ALIPAY(2, "支付宝"), ALIWAPPAY(3, "支付宝H5"), QQPAY(4, "qq钱包"), WXWAPPAY(5, "微信H5"), CPPAY(6, "银联二维码"), CPPAYLARGE(7, "银联二维码大额"), JDPAY(8, "京东支付"), JDWAPPAY(9, "京东wap支付");

	private Integer code;
	private String msg;

	public static String getMsgByCode(Integer code) {
		for (PayChannelEnum ml : PayChannelEnum.values()) {
			if (ml.getcode() == code) {
				return ml.getMsg();
			}
		}
		return "";
	}

	private PayChannelEnum(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public Integer getcode() {
		return code;
	}

	public void setcode(Integer code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
