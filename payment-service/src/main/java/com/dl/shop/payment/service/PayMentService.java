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
import com.dl.member.param.UserBonusParam;
import com.dl.order.api.IOrderService;
import com.dl.order.dto.OrderDTO;
import com.dl.order.param.OrderCondtionParam;
import com.dl.order.param.OrderQueryParam;
import com.dl.order.param.UpdateOrderInfoParam;
import com.dl.shop.payment.core.ProjectConstant;
import com.dl.shop.payment.dao.PayLogMapper;
import com.dl.shop.payment.dao.PayMentMapper;
import com.dl.shop.payment.dto.PaymentDTO;
import com.dl.shop.payment.dto.RspOrderQueryDTO;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.model.PayMent;
import com.dl.shop.payment.pay.common.RspOrderQueryEntity;
import com.dl.shop.payment.pay.rongbao.demo.RongUtil;
import com.dl.shop.payment.pay.yinhe.util.YinHeUtil;

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
    
    @Resource
    private PayLogService payLogService;
    
	@Resource
	private PayLogMapper payLogMapper;
	
	@Resource
	private YinHeUtil yinHeUtil;

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
    public void dealBeyondPayTimeOrder(OrderDTO or) {
    	UpdateOrderInfoParam updateOrderInfoParam = new UpdateOrderInfoParam();
    	updateOrderInfoParam.setOrderSn(or.getOrderSn());
    	updateOrderInfoParam.setOrderStatus(8);//支付失败
    	updateOrderInfoParam.setPayStatus(2);//支付失败
    	updateOrderInfoParam.setPayTime(DateUtil.getCurrentTimeLong());
    	BaseResult<String> updateRst = orderService.updateOrderInfoStatus(updateOrderInfoParam);
    	if(updateRst.getCode() != 0) {
    		log.error("-------------------支付超时订单更新订单为出票失败 异常，返回，code="+updateRst.getCode()+"  msg:"+updateRst.getMsg()+" 订单号："+or.getOrderSn());
    		return;
    	}

		//logger.info("调用第三方订单查询接口 payCode:" + payCode + " payOrderSn:" + payLog.getPayOrderSn());
//    	BaseResult<RspOrderQueryEntity>  baseResult = null;
//    	String payCode = or.getPayCode();
//    	PayLog payLog = new PayLog();
//    	payLog.setOrderSn(or.getOrderSn());
//    	payLog.setPayCode(or.getPayCode());
//    	payLog.setPayType(0);
//    	PayLog payLogDelay = payLogMapper.existPayLog(payLog);
//		if("app_rongbao".equals(payCode)) {
//			baseResult = RongUtil.queryOrderInfo(payLogDelay.getPayOrderSn());
//		}else if("app_weixin".equals(payCode)) {
//			baseResult = yinHeUtil.orderQuery(false,payLog.getPayOrderSn());
//		}
//		if(baseResult.getCode() != 0) {
//			log.error("查询第三方"+payCode+"异常:"+baseResult.getMsg());
//		}
//		RspOrderQueryEntity rspEntity = baseResult.getData();
//		if(rspEntity.isSucc()) {
//			return;//第三方付款成功，就不再回退余额
//		}
    	
    	PayLog updatepayLog = new PayLog();
    	updatepayLog.setIsPaid(ProjectConstant.IS_PAID_FAILURE);
    	updatepayLog.setOrderSn(or.getOrderSn());
    	payLogService.updatePayLogByOrderSn(updatepayLog);
    	
    	SurplusPayParam surplusPayParam = new SurplusPayParam();
    	surplusPayParam.setOrderSn(or.getOrderSn());
    	BaseResult<SurplusPaymentCallbackDTO> rollRst = userAccountService.rollbackUserAccountChangeByPay(surplusPayParam);
    	
    	Integer userBonusId = or.getUserBonusId();
    	if(null != userBonusId) {
    		UserBonusParam userbonusParam = new UserBonusParam();
    		userbonusParam.setUserBonusId(userBonusId);
    		userbonusParam.setOrderSn(or.getOrderSn());
    		userAccountService.rollbackChangeUserAccountByCreateOrder(userbonusParam);
    	}
    	if(rollRst.getCode() != 0) {
    		log.error("-------------------支付超时订单回滚用户余额异常,code="+rollRst.getCode()+"  msg:"+rollRst.getMsg()+" 订单号："+or.getOrderSn());
    	}else {
    		log.info(JSON.toJSONString("用户"+or.getUserId()+"超时支付订单"+or.getOrderSn()+"已回滚账户余额"));
    	}

    }
    
	
}
