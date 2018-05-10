package com.dl.shop.payment.enums;

public enum PayEnums {
	
	PAY_RONGBAO_FAILURE(304035,"支付失败"),
	PAY_RONGBAO_EMPTY(304036,"未查询到该订单信息"), 
	// PAY_WEIXIN_FAILURE(304037,"微信支付失败"), PAY_WEIXIN_FAILURE
	PAY_RONGBAO_NOT_ENOUGH(304038,"提现金额超出提现数值"),
	PAY_RONGBAO_BANK_ERROR(304039,"银行卡信息有误"),
	PAY_RONGBAO_AMT_ERROR(304040,"请提供有效金额信息"),
	PAY_RONGBAO_BANK_QUERY_ERROR(304041,"对不起，请选择有效的很行卡"),
	PAY_STYLE_BLANK(304042,"请提供有效支付方式"),
	PAY_TOTAL_NOTRANGE(304043,"输入金额超出范围"),
	CASH_REVIEWING(304044,"超出提现阈值,进入审核通道"),
	RECHARGE_AMT_ERROR(304045,"对不起，请提供有效的充值金额"),
	RECHARGE_PAY_STYLE_EMPTY(304046,"对不起，请提供有效的充值金额"),
	PAY_YINHE_INNER_ERROR(304047,"银河内部失败");
	
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
