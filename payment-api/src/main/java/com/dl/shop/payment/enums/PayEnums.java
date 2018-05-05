package com.dl.shop.payment.enums;

public enum PayEnums {
	
	PAY_RONGBAO_FAILURE(304035,"融宝支付失败"),
	PAY_RONGBAO_EMPTY(304036,"未查询到该订单信息"),
	PAY_WEIXIN_FAILURE(304037,"微信支付失败");
	
	private Integer code;
    private String msg;

    private PayEnums(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getcode() {
        return code;
    }

    public void setcode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
