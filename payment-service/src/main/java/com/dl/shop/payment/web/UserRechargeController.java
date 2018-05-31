package com.dl.shop.payment.web;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.dl.base.result.BaseResult;
import com.dl.shop.payment.dto.PayLogDTO;
import com.dl.shop.payment.dto.UserRechargeDTO;
import com.dl.shop.payment.dto.YesOrNoDTO;
import com.dl.shop.payment.param.PayLogIdParam;
import com.dl.shop.payment.param.StrParam;
import com.dl.shop.payment.service.UserRechargeService;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/payment/recharge")
public class UserRechargeController {

    @Resource
    private UserRechargeService userRechargeService;
 	
 	/**
     * 	查询当前登录用户是否有充值单
     */
 	@RequestMapping(path="/countUserRecharge", method=RequestMethod.POST)
    public BaseResult<YesOrNoDTO> countUserRecharge(@RequestBody StrParam strParam){
 		return userRechargeService.countUserRecharge();
 	}
 	
}
