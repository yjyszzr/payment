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

	int updateUserWithdrawStatus0To3(UserWithdraw userWithdraw);

	int updateUserWithdrawStatus0To4(UserWithdraw userWithdraw);

	List<UserWithdraw> queryUserWithdrawBySelective(UserWithdraw userWithdraw);

	List<UserWithdraw> queryUserWithdrawByWithDrawSnAndUserId(UserWithdraw userWithdraw);

	List<UserWithdraw> queryUserWithdrawIng();

	int countUserWithdrawByUserId(@Param("userId") Integer userId);

	// 获取提现免审用户的购彩实付金额阀值
	double getUserMoneyPaidForNoCheck();

	// 获取提现免审用户的上限单笔金额
	double getMaxNoCheckMoney();

	// 获取第三方代付公司
	Integer getThirdPayForType();

	Integer queryWithDarwPersonOpen();

	int batchUpdateUserWithDrawSuccess(@Param("userWithdrawSns") List<String> successPersonWithDraw);

	int batchUpdateUserWithDrawFail(@Param("userWithdrawSns") List<String> failPersonWithDraw);

}