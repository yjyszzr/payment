package com.dl.shop.payment.param;

import java.math.BigDecimal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户提现参数类
 * @author zhangzirong
 *
 */
@ApiModel("用户提现参数类")
@Data
public class UserWithdrawParam {

	@ApiModelProperty("提现单号")
    private String withDrawSn;
	
	@ApiModelProperty("提现金额")
    private BigDecimal amount;

	@ApiModelProperty("真实姓名")
    private String realName;

	@ApiModelProperty("银行卡号")
    private String cardNo;
	
	@ApiModelProperty("提现状态 2-失败,1-已完成，0-未完成")
    private String status;			//提现状态 2-失败,1-已完成，0-未完成
}