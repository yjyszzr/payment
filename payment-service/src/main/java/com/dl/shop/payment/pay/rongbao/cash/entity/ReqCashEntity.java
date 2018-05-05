package com.dl.shop.payment.pay.rongbao.cash.entity;

import lombok.Data;

@Data
public class ReqCashEntity {
	String batch_no;
    String batch_count;
    String batch_amount;
    String pay_type;		//紧急程度
    String content;
}
