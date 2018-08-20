package com.dl.shop.payment.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.shop.payment.model.PayBankRecordModel;

public interface PayBankRecordMapper extends Mapper<PayBankRecordModel>{
	
	public List<PayBankRecordModel> listUserBank(PayBankRecordModel payModel);
	
	public List<PayBankRecordModel> listAllUserBank(PayBankRecordModel payModel);
	
	public int updateInfo(PayBankRecordModel payModel);
	
	public int updateIsPaidInfo(PayBankRecordModel payModel);
	
	public List<PayBankRecordModel> queryPayBankRecordModelById(PayBankRecordModel payModel);
	
	public PayBankRecordModel queryPayBankRecordById(@Param("id")Integer id);

	public PayBankRecordModel selectPayBankCardNoByPayLog(PayBankRecordModel payModel);
	public int updatePayBankCardNoByPayLog(PayBankRecordModel payModel);
	
	public PayBankRecordModel selectByBankCardAndPaySuccess(@Param("bankCardNo") String bankCardNo);

	public int updateIspaidRemoveByCardNo(@Param("bankCardNo") String bankCardNo);
}
