package com.dl.shop.payment.dao;

import java.util.List;
import com.dl.base.mapper.Mapper;
import com.dl.shop.payment.model.PayBankRecordModel;

public interface PayBankRecordMapper extends Mapper<PayBankRecordModel>{
	
	public List<PayBankRecordModel> listUserBank(PayBankRecordModel payModel);
	
	public int updateInfo(PayBankRecordModel payModel);
}
