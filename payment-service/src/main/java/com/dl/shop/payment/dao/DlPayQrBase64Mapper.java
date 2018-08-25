package com.dl.shop.payment.dao;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.shop.payment.model.DlPayQrBase64;

public interface DlPayQrBase64Mapper extends Mapper<DlPayQrBase64> {

	Integer saveDlPayQrBase64(DlPayQrBase64 saveBean);
	DlPayQrBase64 selectDlPayQrBase64ById(@Param("id")Integer id);
}