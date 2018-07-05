package com.dl.shop.payment.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.shop.payment.model.UserWithdraw;

public interface UserWithdrawMapper extends Mapper<UserWithdraw> {
	
	int insertUserWithdraw(UserWithdraw userWithdraw);
	
	int updateUserWithdrawBySelective(UserWithdraw userWithdraw);
	
	int updateUserWithdrawStatus3To1(UserWithdraw userWithdraw);
	
	int updateUserWithdrawStatus3To4(UserWithdraw userWithdraw);
	
	int updateUserWithdrawStatus0To2(UserWithdraw userWithdraw);
	
	
	List<UserWithdraw> queryUserWithdrawBySelective(UserWithdraw userWithdraw);
	
	List<UserWithdraw> queryUserWithdrawByWithDrawSnAndUserId(UserWithdraw userWithdraw);
	
	int countUserWithdrawByUserId(@Param("userId") Integer userId);
	
}