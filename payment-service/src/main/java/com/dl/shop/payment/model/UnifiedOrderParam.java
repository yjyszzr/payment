package com.dl.shop.payment.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UnifiedOrderParam {

	private Integer orderNo;
	@ApiModelProperty("支付宝：对一笔交易的具体描述信息。如果是多种商品，请将商品描述字符串累加传给body。微信：商品描述交易字段格式根据不同的应用场景按照以下格式：APP——需传入应用市场上的APP名字-实际商品名称，天天爱消除-游戏充值。")
	private String body;
	@ApiModelProperty("支付宝使用：商品的标题/交易标题/订单标题/订单关键字等")
	private String subject;
	private String ip;
	private double totalAmount;
	@ApiModelProperty("微信扫码支付时使用")
	private String productId;
	@ApiModelProperty("公众号支付用户openid")
	private String openid;
	@ApiModelProperty("页面支付结束后的跳转页面")
	private String returnUrl;
}
