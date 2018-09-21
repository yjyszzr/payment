package com.dl.shop.payment.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.shop.payment.dao.BankUbeyCodeMapper;
import com.dl.shop.payment.dto.BankUbeyCodeDTO;
import com.dl.shop.payment.dto.PayReturnDTO;
import com.dl.shop.payment.dto.BankUbeyCodeDTO.BankCode;
import com.dl.shop.payment.dto.PayReturnUbeyDTO;
import com.dl.shop.payment.model.BankUbeyCodeModel;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.pay.youbei.config.ConfigerUBeyPay;
import com.dl.shop.payment.pay.youbei.entity.FormUBeyEntity;
import com.dl.shop.payment.pay.youbei.util.PayUBeyUtil;
import com.dl.shop.payment.web.PaymentController;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UbeyPayService {
	
	private final static Logger logger = LoggerFactory.getLogger(PaymentController.class);
	@Resource
	private PayUBeyUtil payUBeyUtil;
	@Resource
	private BankUbeyCodeMapper bankUbeyCodeMapper;
	@Resource
	private ConfigerUBeyPay cfgPay;
	
	public BaseResult<PayReturnUbeyDTO> getUBeyPayUrl(PayLog savePayLog, String orderId,String banktype) {
		BaseResult<PayReturnUbeyDTO> payBaseResult = null;
		BigDecimal amtDouble = savePayLog.getOrderAmount();
		BigDecimal bigD = amtDouble.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_EVEN);//！！！！！！！！！！！！！
		String payOrderSn = savePayLog.getPayOrderSn();
		FormUBeyEntity rspEntity = null;
		rspEntity = payUBeyUtil.getUBeyPayUrl(bigD.toString(), payOrderSn,banktype);
		if (rspEntity != null) {
			PayReturnUbeyDTO rEntity = new PayReturnUbeyDTO();
			String url = rspEntity.getUrl();
			
			if (!TextUtils.isEmpty(url)) {
				rEntity.setPayUrl(url);
				rEntity.setPayLogId(savePayLog.getLogId() + "");
				rEntity.setOrderId(orderId);
//				rEntity.setLotteryClassifyId(lotteryClassifyId);
				rEntity.setData(rspEntity.getData());
				rEntity.setSignatrue(rspEntity.getSignature());
				logger.info("Ubeyclient jump url:" + url + " payLogId:" + savePayLog.getLogId() + " orderId:" + orderId);
				payBaseResult = ResultGenerator.genSuccessResult("succ", rEntity);
			} else {
				payBaseResult = ResultGenerator.genFailResult("url decode失败", null);
			}
			
		} else {
			payBaseResult = ResultGenerator.genFailResult("优贝支付返回数据有误");
		}
		return payBaseResult;
	}
	
	public BaseResult<?> getUBeyBankUrl(PayLog savePayLog, String orderId) {
		logger.info("查询Ubey直连网银银行列表getUBeyBank..........");
		BaseResult<?> payBaseResult = null;
		PayReturnDTO rEntity = new PayReturnDTO();
		rEntity.setPayUrl(cfgPay.getBANK_URL()+"?payLogId="+savePayLog.getLogId());
		rEntity.setPayLogId(savePayLog.getLogId() + "");
		rEntity.setOrderId(orderId);
		logger.info("Ubeyclient jump url:" + cfgPay.getBANK_URL() + " payLogId:" + savePayLog.getLogId() + " orderId:" + orderId);
		payBaseResult = ResultGenerator.genSuccessResult("succ", rEntity);
		return payBaseResult;
	}
	
	public BaseResult<BankUbeyCodeDTO> getUBeyBank() {
		logger.info("查询Ubey直连网银银行列表getUBeyBank..........");
		BaseResult<?> payBaseResult = null;
		List<BankUbeyCodeModel> bankUbey = bankUbeyCodeMapper.listUbeyBank(1);
		BankUbeyCodeDTO bankUbeyDTO = new BankUbeyCodeDTO();
		List<BankCode> bankList = new ArrayList<BankUbeyCodeDTO.BankCode>();
		bankUbey.forEach(item->{
			BankCode code = new  BankCode();
			code.setCode(item.getCode());
			code.setImageUrl(item.getImage());
			code.setName(item.getName());
			bankList.add(code);
		});
		bankUbeyDTO.setBank(bankList);
		bankUbeyDTO.setUrl(cfgPay.getUBEYAPI_URL());
		logger.info("Ubey银行列表页面参数BankUbeyCodeDTO={}",bankUbeyDTO);
		return ResultGenerator.genSuccessResult("succ", bankUbeyDTO);
	}
}
