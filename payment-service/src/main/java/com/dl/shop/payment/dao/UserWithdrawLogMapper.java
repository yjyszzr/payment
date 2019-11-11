package com.dl.shop.payment.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.shop.payment.model.UserWithdrawLog;

public interface UserWithdrawLogMapper extends Mapper<UserWithdrawLog> {

	List<UserWithdrawLog> findByWithdrawSn(@Param("withdrawSn")String withdrawSn);

	void batchInsertUserWithDrawLogsSuccess(@Param("userWithdrawSns") List<String> successPersonWithDraw);
	
	void batchInsertUserWithDrawLogsFail(@Param("userWithdrawSns") List<String> failPersonWithDraw);
}