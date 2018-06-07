package com.dl.shop.payment.web;

import javax.annotation.Resource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.dl.base.result.BaseResult;
import com.dl.shop.payment.dto.YesOrNoDTO;
import com.dl.shop.payment.param.StrParam;
import com.dl.shop.payment.param.UserIdParam;
import com.dl.shop.payment.service.UserRechargeService;

@RestController
@RequestMapping("/payment/recharge")
public class UserRechargeController {

    @Resource
    private UserRechargeService userRechargeService;
 	
 	/**
     * 	查询当前登录用户是否有充值单
     */
 	@RequestMapping(path="/countUserRecharge", method=RequestMethod.POST)
    public BaseResult<YesOrNoDTO> countUserRecharge(@RequestBody UserIdParam userIdParam){
 		return userRechargeService.countUserRecharge(userIdParam.getUserId());
 	}
 	
}
