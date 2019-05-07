package com.dl.shop.payment.enums;

public enum PayForCompanyEnum {

	XF_PAYFOR(1, "先锋代付"), TX_PAYFOR1(1809019760, "天下代付数字传奇"), TX_PAYFOR2(1809019662, "天下代付精彩数动"),TX_PAYQDD(2, "Q多多代付");

	private Integer code;
	private String msg;

	public static String getMsgByCode(Integer code) {
		for (PayForCompanyEnum ml : PayForCompanyEnum.values()) {
			if (ml.getCode() == code) {
				return ml.getMsg();
			}
		}
		return "";
	}

	private PayForCompanyEnum(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
