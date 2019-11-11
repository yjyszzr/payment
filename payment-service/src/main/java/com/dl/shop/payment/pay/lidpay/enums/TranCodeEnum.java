package com.dl.shop.payment.pay.lidpay.enums;

public enum TranCodeEnum {

	PAYSCAN(1101, "扫码支付接口"), ORDERQUERY(1102, "订单查询"), BALANCEPAYFOR(2101, "商户余额代付"), BALANCEPAYFOEQUERY(2102, "商户余额代付查询"), BALANCEQUERY(2103, "商户余额查询");

	private Integer code;
	private String msg;

	public static String getMsgByCode(Integer code) {
		for (TranCodeEnum ml : TranCodeEnum.values()) {
			if (ml.getcode() == code) {
				return ml.getMsg();
			}
		}
		return "";
	}

	private TranCodeEnum(int code, String msg) {
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
