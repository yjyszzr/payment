package com.dl.shop.payment.dao;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.shop.payment.model.DlUserReal;

public interface DlUserRealMapper extends Mapper<DlUserReal> {

	DlUserReal findByUserId(@Param("userId") Integer userId);
}