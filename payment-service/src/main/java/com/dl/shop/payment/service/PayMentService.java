package com.dl.shop.payment.service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;
import com.dl.member.api.IUserAccountService;
import com.dl.member.dto.SurplusPaymentCallbackDTO;
import com.dl.member.param.SurplusPayParam;
import com.dl.order.api.IOrderService;
import com.dl.order.dto.OrderDTO;
import com.dl.order.param.OrderCondtionParam;
import com.dl.order.param.OrderQueryParam;
import com.dl.order.param.UpdateOrderInfoParam;
import com.dl.shop.payment.dao.PayMentMapper;
import com.dl.shop.payment.dto.PaymentDTO;
import com.dl.shop.payment.model.PayMent;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PayMentService extends AbstractService<PayMent> {
    @Resource
    private PayMentMapper payMentMapper;
    
    @Resource
    private IOrderService orderService;
    
    @Resource
    private IUserAccountService userAccountService;

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
			paymentDTO.setPayCode(payment.getPayCode());
			paymentDTO.setPayDesc(payment.getPayDesc());
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
    
    /**
     * 处理支付超时订单
     */
    public void dealBeyondPayTimeOrder() {
    	OrderCondtionParam orderQueryParam = new OrderCondtionParam();
    	orderQueryParam.setOrderStatus(1);
    	orderQueryParam.setPayStatus(0);
    	orderQueryParam.setTime(DateUtil.getCurrentTimeLong());
    	BaseResult<List<OrderDTO>> orderDTORst = orderService.queryOrderListByCondition(orderQueryParam);
    	if(orderDTORst.getCode() != 0) {
    		log.error("-------------------查询支付超时订单失败"+orderDTORst.getMsg());
    		return;
    	}
    	
    	List<OrderDTO> orderDTOList = orderDTORst.getData();
    	for(OrderDTO or:orderDTOList) {
    		SurplusPayParam surplusPayParam = new SurplusPayParam();
    		surplusPayParam.setOrderSn(or.getOrderSn());
    		BaseResult<SurplusPaymentCallbackDTO> rollRst = userAccountService.rollbackUserAccountChangeByPay(surplusPayParam);
    		if(rollRst.getCode() != 0) {
    			log.error("-------------------支付超时订单回滚用户余额异常"+orderDTORst.getMsg());
    			continue;
    		}
    		SurplusPaymentCallbackDTO spcd = rollRst.getData();
    		log.info(JSON.toJSONString("用户"+or.getUserId()+"超时支付订单"+or.getOrderSn()+"已回滚账户余额"));
    		
    		UpdateOrderInfoParam updateOrderInfoParam = new UpdateOrderInfoParam();
    		updateOrderInfoParam.setOrderSn(or.getOrderSn());
    		updateOrderInfoParam.setOrderStatus(8);//支付失败
    		updateOrderInfoParam.setPayStatus(2);//支付失败
    		BaseResult<String> updateRst = orderService.updateOrderInfo(updateOrderInfoParam);
    		if(updateRst.getCode() != 0) {
    			log.error("-------------------支付超时订单更新订单为出票失败 异常"+orderDTORst.getMsg());
    			continue;
    		}
    	}
    	 	
    }
    
	
}
