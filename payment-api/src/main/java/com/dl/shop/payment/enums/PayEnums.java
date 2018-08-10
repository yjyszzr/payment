package com.dl.shop.payment.enums;

public enum PayEnums {
	
	PAY_RONGBAO_FAILURE(304035,"支付失败"),
	PAY_RONGBAO_EMPTY(304036,"未查询到该订单信息"), 
	// PAY_WEIXIN_FAILURE(304037,"微信支付失败"), PAY_WEIXIN_FAILURE
	PAY_RONGBAO_NOT_ENOUGH(304038,"提现金额超出提现数值"),
	PAY_RONGBAO_BANK_ERROR(304039,"银行卡信息有误"),
	PAY_RONGBAO_AMT_ERROR(304040,"请提供有效金额信息"),
	PAY_RONGBAO_BANK_QUERY_ERROR(304041,"对不起，请选择有效的银行卡"),
	PAY_CODE_BLANK(304042,"请提供有效支付方式"),
	PAY_TOTAL_NOTRANGE(304043,"输入金额超出范围"),
	CASH_REVIEWING(304044,"超出提现阈值,进入审核通道"),
	RECHARGE_AMT_ERROR(304045,"对不起，请提供有效的充值金额"),
	RECHARGE_PAY_STYLE_EMPTY(304046,"对不起，请提供有效的充值金额"),
	PAY_YINHE_INNER_ERROR(304047,"银河内部失败"),
	CASH_FAILURE(304048,"银河内部失败"),
	WITHDRAW_EMPTY(304049,"提现流水为空"),
	WITHDRAW_USER_ACC_EMPTY(304050,"提现查询user表为空"),
	CASH_USER_MOENY_REDUC_ERROR(304051,"用户余额预扣除失败"),
	PAY_RONGBAO_LOW_LIMIT(304052,"最低提现金额为3元"),
	PAY_TOKEN_EXPRIED(304053,"支付信息已失效，请返回重新支付"),
	PAY_TOKEN_EMPTY(304054,"支付信息有误,请重新返回支付"),
	PAY_RECHARGE_MAX(304055,"当前支付方式限额10万/笔"),
	PAY_WITHDRAW_APPLY_SUC(304056,"提现申请已提交"),
	PAY_WITHDRAW_BIND_CARD_RETRY(304057,"获取BankCode失败，请重新绑卡"),
	PAY_DBDATA_IS_NOT_IN(304058,"数据库不存在该数据"),
	PAY_XIANFENG_FAILURE(304059,"支付失败"),
	PAY_XIANFENG_ORDER_BLANK(304060,"支付订单查询失败"),
	PAY_XIANFENG_SMS_ERROR(304061,"验证码获取失败"),
	PAY_XIANFENG_SMS_EXCEPTION(304062,"获取验证码信息异常"),
	PAY_XIANFENG_BANKTYPE_FAILURE(304063,"银行卡查询失败"),
	PAY_XIANFENG_BANKTYPE_UNKNOW(304064,"银行卡类型未知"),
	PAY_XIANFENG_PAY_ERROR(304065,"请求支付失败"),
	PAY_MAX_COUNT_WITHDRAW(304066,"一天提现不能超过1次"),
	PAY_XIANFENG_VERIFYCODE_WRONG(304067,"请输入正确的验证码"),
	PAY_XIANFENG_VERIFYCODE_INVALID(304068,"验证码已失效，请重新获取"),
	PAY_WITHDRAW_REPEAT(304069,"用户提现过于频繁"),
	PAY_XIANFENG_QUICKPAY_PHONE_ERROR(304070,"手机号格式不正确"),
	PAY_XIANFENG_QUICKPAY_CARDNO_ERROR(304071,"卡号格式不正确"),
	PAY_XIANFENG_QUICKPAY_ID_ERROR(304072,"身份证格式不正确"),
	PAY_XIANFENG_BANK_INFO_ERROR(304073,"因银行信息填写错误,订单已取消请重新下单"),
	PAY_XIANFENG_BANK_PAY_DOING(304074,"支付处理中"),
	PAY_XIANFENG_CANCEL_ERROR(304075,"订单已取消请重新下单"),
	PAY_THREE_COUNT_WITHDRAW(304076,"一天提现不能超过3次");
	
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
