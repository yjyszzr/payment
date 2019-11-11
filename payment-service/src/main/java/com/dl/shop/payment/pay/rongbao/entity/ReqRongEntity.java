package com.dl.shop.payment.pay.rongbao.entity;

import java.io.Serializable;
import lombok.Data;

@Data
public class ReqRongEntity implements Serializable{
	private static final long serialVersionUID = 1L;
	private String orderId;  //订单ID
	private String pName;	 //商品名称
	private String pDesc;	 //商品描述
	private Double total;	 //订单金额
	private String userId;	 //用户ID
	private String transTime;//交易时间
}
