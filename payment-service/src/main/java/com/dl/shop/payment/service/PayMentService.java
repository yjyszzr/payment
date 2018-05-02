package com.dl.shop.payment.service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.service.AbstractService;
import com.dl.shop.payment.dao.PayMentMapper;
import com.dl.shop.payment.dto.PaymentDTO;
import com.dl.shop.payment.model.PayMent;

@Service
public class PayMentService extends AbstractService<PayMent> {
    @Resource
    private PayMentMapper payMentMapper;

    /**
     * 查询所有可用的支付方式
     * @return
     */
    public BaseResult<List<PaymentDTO>> findAllDto() {
		List<PayMent> payments = super.findAll();
		if(CollectionUtils.isEmpty(payments)) {
			return ResultGenerator.genSuccessResult("success", new ArrayList<PaymentDTO>(0));
		}
		List<PaymentDTO> list = payments.stream().filter(payment->payment.getIsEnable() == 1).map(payment->{
			PaymentDTO paymentDTO = new PaymentDTO();
			paymentDTO.setIsEnable(payment.getIsEnable());
			paymentDTO.setPayCode(payment.getPayCode());
			paymentDTO.setPayDesc(payment.getPayDesc());
			paymentDTO.setPayFee(payment.getPayFee());
			paymentDTO.setPayId(payment.getPayId());
			paymentDTO.setPayName(payment.getPayName());
			paymentDTO.setPaySort(payment.getPaySort());
			paymentDTO.setPayType(payment.getPayType());
			paymentDTO.setPayTitle(payment.getPayTitle());
			paymentDTO.setPayImg(payment.getPayImg());
			return paymentDTO;
		}).collect(Collectors.toList());
		return ResultGenerator.genSuccessResult("success", list);
	}
    /**
     * 通过payCode读取可用支付方式
     * @param payCode
     * @return
     */
    public BaseResult<PaymentDTO> queryByCode(String payCode) {
		List<PaymentDTO> paymentDTOs = this.findAllDto().getData();
		Optional<PaymentDTO> optional = paymentDTOs.stream().filter(dto-> dto.getPayCode().equals(payCode)).findFirst();
		return optional.isPresent()?ResultGenerator.genSuccessResult("success", optional.get()):ResultGenerator.genFailResult("没有匹配的记录！");
	}
    
	
}
