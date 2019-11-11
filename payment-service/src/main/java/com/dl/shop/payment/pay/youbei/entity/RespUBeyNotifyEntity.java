package com.dl.shop.payment.pay.youbei.entity;

import lombok.Data;

@Data
public class RespUBeyNotifyEntity {
	
	public String  respInfo;
	public String  amount;
	public String  orderId;
	public String  respCode;
	public String  type;
	public String  userid;
	
}
