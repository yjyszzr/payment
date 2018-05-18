package com.dl.shop.payment.service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.util.TextUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson.JSON;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;
import com.dl.member.api.IUserAccountService;
import com.dl.member.dto.SurplusPaymentCallbackDTO;
import com.dl.member.param.SurplusPayParam;
import com.dl.member.param.UserAccountParamByType;
import com.dl.member.param.UserBonusParam;
import com.dl.order.api.IOrderService;
import com.dl.order.dto.OrderDTO;
import com.dl.order.param.OrderSnParam;
import com.dl.order.param.UpdateOrderInfoParam;
import com.dl.shop.payment.core.ProjectConstant;
import com.dl.shop.payment.dao.PayLogMapper;
import com.dl.shop.payment.dao.PayMentMapper;
import com.dl.shop.payment.dto.PaymentDTO;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.model.PayMent;
import com.dl.shop.payment.param.RollbackOrderAmountParam;
import com.dl.shop.payment.pay.rongbao.demo.RongUtil;
import com.dl.shop.payment.pay.rongbao.entity.ReqRefundEntity;
import com.dl.shop.payment.pay.rongbao.entity.RspRefundEntity;
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

	@Resource
	private RongUtil rongUtil;
	
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
    @Transactional
    public void dealBeyondPayTimeOrder(OrderDTO or) {
    	SurplusPayParam surplusPayParam = new SurplusPayParam();
    	surplusPayParam.setOrderSn(or.getOrderSn());
    	BaseResult<SurplusPaymentCallbackDTO> rollRst = userAccountService.rollbackUserAccountChangeByPay(surplusPayParam);
    	if(rollRst.getCode() != 0) {
    		log.error(rollRst.getMsg());
    		return;
    	}
    	
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
    	
    	UpdateOrderInfoParam updateOrderInfoParam = new UpdateOrderInfoParam();
    	updateOrderInfoParam.setOrderSn(or.getOrderSn());
    	updateOrderInfoParam.setOrderStatus(8);//订单失败
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

    }
    
    /**
     * 资金回滚
     * @param param
     * @return
     */
	public BaseResult<?> rollbackOrderAmount(RollbackOrderAmountParam param) {
		log.info("in rollbackOrderAmount ordersn=" + param.getOrderSn());
		String orderSn = param.getOrderSn();
		OrderSnParam snParam = new OrderSnParam();
		snParam.setOrderSn(orderSn);
		BaseResult<OrderDTO> orderRst = orderService.getOrderInfoByOrderSn(snParam);
		if(orderRst.getCode() != 0) {
			log.info("orderService.getOrderInfoByOrderSn rst code="+orderRst.getCode()+" msg="+orderRst.getMsg());
			return ResultGenerator.genFailResult();
		}
		OrderDTO order = orderRst.getData();
		BigDecimal surplus = order.getSurplus();
		BigDecimal bonusAmount = order.getBonus();
		BigDecimal moneyPaid = order.getMoneyPaid();
		String payName = order.getPayName();
		BigDecimal thirdPartyPaid = order.getThirdPartyPaid();
		//第三方支付
		int payType = 1;//默认微信
		if(surplus != null && surplus.doubleValue() > 0) {
			payType = 2;
		}
		boolean hasThird = false;
		if(thirdPartyPaid != null && thirdPartyPaid.doubleValue() > 0) {
			hasThird = true;
		}
		if(hasThird && payType == 2) {
			payType = 3;
		}
		boolean succThird = false;
		log.info("出票失败含有第三方支付:" + hasThird);
		if(hasThird) {
			orderSn = order.getOrderSn();
			log.info("出票失败含有第三方支付 订单orderSn:" + orderSn);
			if(!TextUtils.isEmpty(orderSn)) {
				PayLog payLog = payLogService.findPayLogByOrderSn(orderSn);
				if(payLog == null) {
					return ResultGenerator.genFailResult("回滚订单不存在 orderSn:" + orderSn);
				}
				String payCode = payLog.getPayCode();
				log.info("回滚查询PayLog信息:" + " payCode:" + payCode + " payOrderSn:" + payLog.getPayOrderSn());
				RspRefundEntity rspRefundEntity = null;
				if(payLog != null) {
					if(payCode.equals("app_rongbao")) {
						ReqRefundEntity reqEntity = new ReqRefundEntity();
						reqEntity.setAmount(thirdPartyPaid.toString());
						reqEntity.setNote("出票失败退款操作");
						reqEntity.setOrig_order_no(payLog.getPayOrderSn());
						try {
							rspRefundEntity = rongUtil.refundOrderInfo(reqEntity);
							log.info("rEntity:" + rspRefundEntity.toString());
							if(rspRefundEntity != null && rspRefundEntity.isSucc()) {
								succThird = true;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}else if(payCode.equals("app_weixin") || "app_weixin_h5".equals(payCode)){
						boolean isInWeChat = "app_weixin_h5".equals(payCode);
						String amt = thirdPartyPaid.toString();
						BigDecimal bigDec = new BigDecimal(amt);
						String amtFen = bigDec.movePointRight(2).intValue()+"";
						log.info("=========================");
						log.info("进入到了微信订单回滚 isInWeChat：" + isInWeChat + " amtFen" + amtFen + "payOrderSn:" + payLog.getPayOrderSn());
						rspRefundEntity = yinHeUtil.orderRefund(isInWeChat,payLog.getPayOrderSn(),amtFen);
						if(rspRefundEntity.isSucc()) {
							succThird = true;
						}
						log.info("微信订单回滚 isSucc:" + rspRefundEntity.isSucc() + " msg:" + rspRefundEntity.toString());
						log.info("=========================");
					}
					//第三方资金退回
					if(succThird) {
						log.info("第三方资金退回成功 payCode：" + payCode + " amt:" + thirdPartyPaid.toString());
						//===========记录退款流水==========
						UserAccountParamByType userAccountParamByType = new UserAccountParamByType();
						Integer accountType = ProjectConstant.BUY;
						log.info("===========更新用户流水表=======:" + accountType);
						userAccountParamByType.setAccountType(accountType);
						userAccountParamByType.setAmount(new BigDecimal(payLog.getOrderAmount().doubleValue()));
						userAccountParamByType.setBonusPrice(BigDecimal.ZERO);//暂无红包金额
						userAccountParamByType.setOrderSn(payLog.getOrderSn());
						userAccountParamByType.setPayId(payLog.getLogId());
						if(payCode.equals("app_weixin") || payCode.equals("app_weixin_h5")) {
							payName = "微信";
						}else {
							payName = "银行卡";
						}
						userAccountParamByType.setPaymentName(payName);
						userAccountParamByType.setThirdPartName(payName);
						userAccountParamByType.setThirdPartPaid(new BigDecimal(payLog.getOrderAmount().doubleValue()));
						userAccountParamByType.setUserId(payLog.getUserId());
						BaseResult<String> accountRst = userAccountService.insertUserAccount(userAccountParamByType);
						if(accountRst.getCode() == 0) {
							log.info("退款成功记录流水成功...");
						}
					}else {
						payLog.setPayMsg("第三方资金退回失败");
						payLogService.update(payLog);
						log.info("第三方资金退回失败 payCode：" + payCode + " amt:" + thirdPartyPaid.toString());
					}
				}
			}
		}else {	//无第三方支付，默认第三支付成功
			succThird = true;
		}
		if(succThird && (payType ==2 || payType == 3)) {
			SurplusPayParam surplusPayParam = new SurplusPayParam();
			surplusPayParam.setOrderSn(orderSn);
			surplusPayParam.setSurplus(surplus);
			surplusPayParam.setBonusMoney(bonusAmount);
			surplusPayParam.setPayType(payType);
			surplusPayParam.setMoneyPaid(moneyPaid);
			surplusPayParam.setThirdPartName(payName);
			surplusPayParam.setThirdPartPaid(thirdPartyPaid);
			BaseResult<SurplusPaymentCallbackDTO> rollbackUserAccountChangeByPay = userAccountService.rollbackUserAccountChangeByPay(surplusPayParam);
			log.info(" orderSn="+orderSn+" , Surplus="+surplus.doubleValue()+" rollbackOrderAmount回滚用户余额结束！ 订单回调返回结果：status=" + rollbackUserAccountChangeByPay.getCode()+" , message="+rollbackUserAccountChangeByPay.getMsg());
			if(rollbackUserAccountChangeByPay.getCode() != 0) {
				log.info(" orderSn="+orderSn+" , Surplus="+surplus.doubleValue()+" rollbackOrderAmount回滚用户余额时出错！");
			}
		}
		return ResultGenerator.genSuccessResult();
	}
}
