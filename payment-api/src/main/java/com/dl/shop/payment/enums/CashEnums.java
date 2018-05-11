package com.dl.shop.payment.enums;

/**
 * 提现状态类
 */
public enum CashEnums {
	
	CASH_APPLY(1,"申请"),
	CASH_REVIEWING(2,"提现审核中"),
	CASH_SUCC(3,"提现成功"),
	CASH_FAILURE(4,"提现失败");
	
	private Integer code;
    private String msg;
    
    private CashEnums(int code, String msg) {
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
