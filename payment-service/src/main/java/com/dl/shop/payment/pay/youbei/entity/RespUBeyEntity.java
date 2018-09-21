package com.dl.shop.payment.pay.youbei.entity;

import lombok.Data;

@Data
public class RespUBeyEntity {
	
	public String  fee;
	public String  message;
	public String  money;
	public String  orderId;
	public String  orderId_state;
	public String  state;
	public String  type;
	public String  userid;
}
