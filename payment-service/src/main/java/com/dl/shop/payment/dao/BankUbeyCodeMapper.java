package com.dl.shop.payment.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.shop.payment.model.BankUbeyCodeModel;

public interface BankUbeyCodeMapper extends Mapper<BankUbeyCodeModel>{
	
	public List<BankUbeyCodeModel> listUbeyBank(@Param("isShow")Integer isShow);
	
}
