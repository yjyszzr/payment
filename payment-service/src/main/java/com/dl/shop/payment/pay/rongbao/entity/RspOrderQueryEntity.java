package com.dl.shop.payment.pay.rongbao.entity;

import java.io.Serializable;
import org.apache.http.util.TextUtils;
import lombok.Data;

@Data
public class RspOrderQueryEntity implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String merchant_id;
	private String order_no;
	private String result_code;
	private String result_msg;
	private String status;
	private String timestamp;
	private String total_fee;
	private String trade_no;
	
	//微信支付使用
	private Integer tradeState;
	private String tradeStateDesc;
	private String tradeNo;
	private Integer tradeEndTime;
	
	public boolean isSucc() {
		if(!TextUtils.isEmpty(result_code)) {
			return result_code.equals("0000");
		}
		return false;
	}
}
