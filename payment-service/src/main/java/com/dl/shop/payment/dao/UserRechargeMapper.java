package com.dl.shop.payment.dao;

import java.util.List;

import com.dl.base.mapper.Mapper;
import com.dl.shop.payment.model.UserRecharge;

public interface UserRechargeMapper extends Mapper<UserRecharge> {
	
	int insertUserRecharge(UserRecharge userRecharge);
	
	int updateUserRechargeBySelective(UserRecharge userRecharge);
	
	List<UserRecharge> queryUserChargeBySelective(UserRecharge userRecharge);
	
	int countUserCharge(Integer userId);
}