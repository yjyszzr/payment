package com.dl.shop.payment.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.shop.payment.model.UserWithdraw;

public interface UserWithdrawMapper extends Mapper<UserWithdraw> {
	
	int insertUserWithdraw(UserWithdraw userWithdraw);
	
	int updateUserWithdrawBySelective(UserWithdraw userWithdraw);
	
	int updateUserWithdrawStatus0To1(UserWithdraw userWithdraw);
	
	int updateUserWithdrawStatus0To4(UserWithdraw userWithdraw);
	
	
	List<UserWithdraw> queryUserWithdrawBySelective(UserWithdraw userWithdraw);
	
	List<UserWithdraw> queryUserWithdrawByWithDrawSnAndUserId(UserWithdraw userWithdraw);
	
	int countUserWithdrawByUserId(@Param("userId") Integer userId);
	
}