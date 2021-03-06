package com.dl.shop.payment.web;

import io.swagger.annotations.ApiOperation;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dl.base.result.BaseResult;
import com.dl.base.util.DateUtil;
import com.dl.lottery.api.ILotteryPrintService;
import com.dl.member.api.IUserAccountService;
import com.dl.order.api.IOrderService;
import com.dl.order.param.UpdateOrderPayStatusParam;
import com.dl.shop.payment.dto.RspOrderQueryDTO;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.model.WxpayNotifyModel;
import com.dl.shop.payment.pay.common.RspOrderQueryEntity;
import com.dl.shop.payment.pay.yinhe.config.ConfigerPay;
import com.dl.shop.payment.pay.yinhe.entity.RspNotifyWeChatEntity;
import com.dl.shop.payment.service.PayLogService;
import com.dl.shop.payment.service.PayMentService;
import com.dl.shop.payment.service.UserRechargeService;
import com.dl.shop.payment.utils.XmlUtil;

@Controller
@RequestMapping("/payment/wxpay")
public class WxpayNotifyController {
	//url /payment/wxpay/notify
	private final static Logger logger = LoggerFactory.getLogger(WxpayNotifyController.class);
	@Resource
	private PayLogService payLogService;
	@Autowired
	private IUserAccountService userAccountService;
	@Autowired
	private IOrderService orderService;
	@Autowired
	private UserRechargeService userRechargeService;
	@Resource
	private ConfigerPay cfgPay;
	@Resource
	private PayMentService paymentService;
	@Resource
	private ILotteryPrintService lotteryPrintService;
	
	@ApiOperation(value="微信支付回调")
	@PostMapping("notify")
	public void payNotify(HttpServletRequest request, HttpServletResponse response) {
		logger.warn(" in controller /payment/wxpay/notify");
		String val = request.getParameter("result");
		RspNotifyWeChatEntity rspEntity = null;
		if(TextUtils.isEmpty(val)) {
			val = request.getParameter("returnCode");
			String transNo = request.getParameter("transNo");
			String amt = request.getParameter("amt");
			String transTime = request.getParameter("transTime");
			if(!TextUtils.isEmpty(val)) {
				rspEntity = new RspNotifyWeChatEntity();
				rspEntity.code = val;
				rspEntity.transNo = transNo;
				rspEntity.amt = amt;
				rspEntity.transTime = transTime;
			}
		}
		logger.info("微信回掉处理:" + val + " rspEntity:" + rspEntity);
		//微信内部H5支付处理逻辑
		if(rspEntity != null) {
			if(rspEntity.isSucc()) {
				String transNo = rspEntity.transNo;
				//获取payLog信息
				PayLog payLog = payLogService.findPayLogByOrderSign(transNo);
				if(payLog != null) {
//					String payCode = payLog.getPayCode();
					String amt = payLog.getOrderAmount().movePointRight(2).intValue()+"";
					int isPaid = payLog.getIsPaid();
					if(!TextUtils.isEmpty(amt) && amt.equals(rspEntity.amt)) {
						if(isPaid == 1) {
							logger.info("该订单已支付更新~");
							return;
						}
						logger.info("处理订单相关信息...");
						RspOrderQueryEntity rspQueryEntity = new RspOrderQueryEntity();
						rspQueryEntity.setResult_code("0000");
						rspQueryEntity.setPayCode("app_weixin");
						rspQueryEntity.setTrade_no(transNo);
						operation(payLog,transNo,response,rspQueryEntity);
					}
				}else {
					logger.info("payOrderSn="+transNo+"微信内部H5支付失败 订单查询payLog=空");
				}
			}
		}else {	//微信外部回调处理逻辑
			String requestStr = val;
			logger.warn("requestStr="+requestStr+ "  /payment/wxpay/notify requestStr 微信支付 验证信息: " + requestStr);
			WxpayNotifyModel responseModel = null;
			try {
				responseModel = XmlUtil.xmlToBean(requestStr, WxpayNotifyModel.class);
			} catch (Exception e1) {
				logger.error("微信外部回调处理异常",e1);
			}

			String resultCode = null;
			if(null != responseModel) {
				resultCode = responseModel.getResult_code();
			}
			if ("SUCCESS".equals(resultCode)) {
				// 获取回调的具体参数
				String appid = responseModel.getAppid();//prePayJo.getString("appid");
				String mchId = responseModel.getMch_id();//prePayJo.getString("mch_id");
				String bank_type = responseModel.getBank_type();//prePayJo.getString("bank_type");
				String payOrderSn = responseModel.getOut_trade_no();//prePayJo.getString("out_trade_no");
				logger.info(" payOrderSn="+payOrderSn+"开始回调接口处理");
				String tradeNo = responseModel.getTransaction_id();//prePayJo.getString("transaction_id");
				int amount = responseModel.getTotal_fee();
				PayLog payLog = payLogService.findPayLogByOrderSign(payOrderSn);
				if(null == payLog) {
					logger.info("payOrderSn="+payOrderSn + " payLog对象未查询到，返回失败！");
					//fail
					String xml = "<xml><return_code><![CDATA[FAIL]]></return_code> <return_msg><![CDATA[order no find]]></return_msg></xml>";
					try {
						response.getWriter().write(xml);
					} catch (IOException e) {
						logger.error("payOrderSn={},银河微信回调 write return error",payOrderSn,e);
					}
					return;
				}
				int isPaid = payLog.getIsPaid();
				logger.info("=======isPaid:" + isPaid +" payLogId:" +payLog.getLogId() +"==========");
				if(1== isPaid) {
					logger.info("payOrderSn="+payOrderSn+ " paylog.ispaid=1,已支付成功，返回OK！");
					String xml = "<xml><return_code><![CDATA[SUCCESS]]></return_code> <return_msg><![CDATA[OK]]></return_msg></xml>";
					try {
						response.getWriter().write(xml);
					} catch (IOException e) {
						logger.error("payOrderSn={},银河微信回调 write return error",payOrderSn,e);
					}
					return;
				}
				BigDecimal orderAmount = payLog.getOrderAmount().multiply(BigDecimal.valueOf(100)).setScale(0,RoundingMode.HALF_EVEN);
				logger.info("实际交易金额:" + amount +" 订单金额:" + orderAmount);
				if (((amount == Integer.parseInt(orderAmount.toString())) || "true".equals(cfgPay.getDEBUG())) && (appid.equals(cfgPay.getAPPID()))) {
					logger.info("payOrderSn="+payOrderSn+"订单金额或appid,mchId校验成功，前去回调订单服务！");
					RspOrderQueryEntity rspOrderQueryEntity = new RspOrderQueryEntity();
					rspOrderQueryEntity.setResult_code("0000");
					rspOrderQueryEntity.setPayCode("app_weixin");
					rspOrderQueryEntity.setTrade_no(payOrderSn);
					rspOrderQueryEntity.setTotal_fee(amount+"");
					this.operation(payLog,tradeNo,response,rspOrderQueryEntity);
				}else {
					logger.info("payOrderSn="+payLog.getPayOrderSn()+ " 订单金额或appid,mchId校验失败！");
				}
			}
		}
	}
	
	/**
	 * 处理PayLog,Log相关流水信息,回调订单相关逻辑
	 * @param payLog
	 * @param loggerId
	 * @param tradeNo
	 * @param response
	 */
	private void operation(PayLog payLog,String tradeNo,
			HttpServletResponse response,RspOrderQueryEntity rspEntity) {
		try {
			int payType = payLog.getPayType();
			boolean rst = false;
			if(0 == payType) {
				paymentService.orderOptions(payLog,rspEntity);
				/*if(rspEntity.isSucc()) {
					rst = this.orderOptionsSucc(tradeNo, payLog, loggerId, rspEntity);
				}*/
			} else if(1 == payType){
				BaseResult<RspOrderQueryDTO> result = paymentService.rechargeOptions(payLog,rspEntity);
				if(result.getCode() == 0) {
					rst = true;
				}
			}
			if(rst) {
				String xml = "<xml><return_code><![CDATA[SUCCESS]]></return_code> <return_msg><![CDATA[OK]]></return_msg></xml>";
				response.getWriter().write(xml);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/***
	 * 订单支付，微信回调成功
	 */
	private boolean orderOptionsSucc(String tradeNo,PayLog payLog,String loggerId, RspOrderQueryEntity rspEntity) {
		boolean isSucc = true;
		int currentTime = DateUtil.getCurrentTimeLong();
		//更新order
		UpdateOrderPayStatusParam param = new UpdateOrderPayStatusParam();
		param.setPayStatus(1);
		param.setPayTime(currentTime);
//		param.setPayId(payId);
		param.setPaySn(payLog.getLogId()+"");
		param.setPayName(payLog.getPayName());
		param.setPayCode(payLog.getPayCode());
		param.setOrderSn(payLog.getOrderSn());
		BaseResult<Integer> updateOrderInfo = orderService.updateOrderPayStatus(param);
		
		logger.info("==============支付成功订单回调[orderService]==================");
		logger.info("payLogId:" + payLog.getLogId() + " payName:" + payLog.getPayName() 
		+ " payCode:" + payLog.getPayCode() + " payOrderSn:" + payLog.getPayOrderSn());
		logger.info("==================================");
		if(updateOrderInfo.getCode() == 0) {
			PayLog updatePayLog = new PayLog();
			updatePayLog.setPayTime(currentTime);
			payLog.setLastTime(currentTime);
			updatePayLog.setTradeNo(rspEntity.getTrade_no());
			updatePayLog.setLogId(payLog.getLogId());
			updatePayLog.setIsPaid(1);
			updatePayLog.setPayMsg("支付成功");
			payLogService.update(updatePayLog);
		}else {
			logger.error(loggerId+" paylogid="+"ordersn=" + payLog.getOrderSn()+"更新订单成功状态失败");
			return false;
		}
		return isSucc;
	}

	/***
	 * 充值，微信回调
	 */
	/*private boolean recharageOptionSucc(String tradeNo,PayLog payLog) {
		boolean isSucc = false;
		int currentTime = DateUtil.getCurrentTimeLong();
		//更新充值单信息
		UpdateUserRechargeParam updateUserRechargeParam = new UpdateUserRechargeParam();
		updateUserRechargeParam.setPaymentCode(payLog.getPayCode());
		updateUserRechargeParam.setPaymentId(payLog.getLogId()+"");
		updateUserRechargeParam.setPaymentName(payLog.getPayName());
		updateUserRechargeParam.setPayTime(currentTime);
		updateUserRechargeParam.setStatus("1");
		updateUserRechargeParam.setRechargeSn(payLog.getOrderSn());
		userRechargeService.updateReCharege(updateUserRechargeParam);
		
		//给用户增加不可提现余额,注意:rechargeUserMoneyLimit 已經生成充值流水userAccountService
		RecharegeParam recharegeParam = new RecharegeParam();
		recharegeParam.setAmount(payLog.getOrderAmount());
		recharegeParam.setPayId(tradeNo);
		recharegeParam.setThirdPartName("微信");
		recharegeParam.setThirdPartPaid(payLog.getOrderAmount());
		recharegeParam.setUserId(payLog.getUserId());
		BaseResult<String>  rechargeRst = userAccountService.rechargeUserMoneyLimit(recharegeParam);
		if(rechargeRst.getCode() != 0) {
			logger.error("给个人用户充值：code"+rechargeRst.getCode() +"message:"+rechargeRst.getMsg());
		}else {
			isSucc = true;
		}
		return isSucc;
	}*/
}
