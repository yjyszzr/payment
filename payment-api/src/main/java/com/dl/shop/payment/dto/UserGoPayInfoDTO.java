package com.dl.shop.payment.dto;

import java.util.List;

import com.dl.member.dto.UserBonusDTO;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UserGoPayInfoDTO {

	@ApiModelProperty(value="订单信息token")
	private String payToken;
	@ApiModelProperty(value="订单金额")
	private String orderMoney;
	@ApiModelProperty(value="余额抵扣")
	private String surplus;
	@ApiModelProperty(value="优惠卷抵扣")
	private String bonusAmount;
	@ApiModelProperty(value="当前优惠卷")
	private String bonusId;
	@ApiModelProperty(value="需第三方支付")
	private String thirdPartyPaid;
	@ApiModelProperty(value="用户可用优惠卷列表")
	private List<UserBonusDTO> bonusList;
	@ApiModelProperty(value="彩票种类：1:竞彩足球,2大乐透,3	竞彩篮球,4	快3,5双色球,6北京单场,7广东11选5,8更多彩种")
	private String lotteryClassifyId;
	@ApiModelProperty(value="有效红包数量")
	private int bonusNumber;
	@ApiModelProperty(value="超门槛红包提示")
	private String bonusDesc;
}
