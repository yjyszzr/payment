package com.dl.shop.payment.web;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

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
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.util.DateUtil;
import com.dl.member.api.IUserAccountService;
import com.dl.member.param.UpdateUserRechargeParam;
import com.dl.order.api.IOrderService;
import com.dl.order.param.UpdateOrderInfoParam;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.pay.rongbao.config.ReapalH5Config;
import com.dl.shop.payment.pay.rongbao.entity.PayResultEntity;
import com.dl.shop.payment.pay.rongbao.util.DecipherH5;
import com.dl.shop.payment.pay.rongbao.util.Md5Utils;
import com.dl.shop.payment.service.PayLogService;

import io.swagger.annotations.ApiOperation;

/***
 * 融宝callback
 * @date 2018.04.28
 */
@Controller
@RequestMapping("/rongbaopay")
public class RongbaoPlayController extends AbstractBaseController{
	private final static Logger logger = LoggerFactory.getLogger(PaymentController.class);
	
	@Resource
	private PayLogService payLogService;
	@Autowired
	private IUserAccountService userAccountService;
	@Autowired
	private IOrderService orderService;
	
	@ApiOperation(value="融宝支付回调")
	@PostMapping("callback")
	public void payCallBack(HttpServletRequest request, HttpServletResponse response) {
		String key = ReapalH5Config.key;
		String merchantId = request.getParameter("merchant_id");
		String data = request.getParameter("data");
		String encryptkey = request.getParameter("encryptkey");
		System.out.println("資金方回调... data:" + data);
		if(!TextUtils.isEmpty(data) && !TextUtils.isEmpty(encryptkey)) {
			logger.info("资金方返回原key:" + encryptkey);
			logger.info("资金方返回原数据:" + data);
			//解密返回数据
			String decryData = decodeRspInfo(data,encryptkey);
			logger.info("数据解密结果:" + decryData);
			if(!TextUtils.isEmpty(decryData)) {
				//获取融宝支付的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
				JSONObject jsonObject = JSON.parseObject(decryData);	
				//返回sign
				String sign = jsonObject.getString("sign");
				//验签sign
				String mysign = decodeRspSign(jsonObject,key);
				logger.info("验签:" + mysign);
				logger.info("返回sign:" + sign);
				boolean succ = sign.equals(mysign);
				if(succ) {
					//jsonObject -> 转换业务实体类
					logger.info("验签成功...");
					PayResultEntity rEntity = JSON.parseObject(jsonObject.toJSONString(),PayResultEntity.class);
					//更新订单信息
					String orderId = rEntity.order_no;
					PayLog payLog = payLogService.findPayLogByOrderSign(orderId);
					if(null == payLog) {
						logger.info(rEntity.order_no + " payLog对象未查询到，返回失败！");
						//fail
						String xml = "<xml><return_code><![CDATA[FAIL]]></return_code> <return_msg><![CDATA[order no find]]></return_msg></xml>";
						try {
							response.getWriter().write(xml);
						} catch (IOException e) {
							e.printStackTrace();
						}
						OutputStream out;
						try {
							out = response.getOutputStream();
							out.write("success".getBytes());
							out.flush();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}else {
						call(rEntity,payLog,request,response);
						OutputStream out;
						try {
							out = response.getOutputStream();
							out.write("success".getBytes());
							out.flush();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}else {
					logger.info("验签失败...");
				}
			}
		}else {
			logger.info("资金方回调参数错误 data:" + data +" encryptkey:" + encryptkey);
		}
	}
	
	/***
	 * 第三方回调订单逻辑
	 * @param rEntity
	 * @param payLog
	 * @param request
	 * @param response
	 */
	private void call(PayResultEntity rEntity,PayLog payLog,HttpServletRequest request, HttpServletResponse response) {
		int isPaid = payLog.getIsPaid();
		String loggerId = payLog.getPayOrderSn();
		if(1== isPaid) {
			logger.info(payLog.getPayOrderSn() + " paylog.ispaid=1,已支付成功，返回OK！");
			String xml = "<xml><return_code><![CDATA[SUCCESS]]></return_code> <return_msg><![CDATA[OK]]></return_msg></xml>";
			try {
				response.getWriter().write(xml);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		int orderAmount = (int)(payLog.getOrderAmount().doubleValue()*100);
		try {
			int payType = payLog.getPayType();
			int currentTime = DateUtil.getCurrentTimeLong();
			boolean result = false;
			if(0 == payType) {
				//order
				UpdateOrderInfoParam param = new UpdateOrderInfoParam();
				param.setPayStatus(1);
				param.setOrderStatus(1);
				param.setPayTime(currentTime);
				param.setPaySn(payLog.getLogId()+"");
				param.setPayName(payLog.getPayName());
				param.setPayCode(payLog.getPayCode());
				param.setOrderSn(payLog.getOrderSn());
				BaseResult<String> baseResult = orderService.updateOrderInfo(param);
				logger.info(loggerId + " 订单回调返回结果：status=" + baseResult.getCode()+" , message="+baseResult.getMsg());
				if(0 == baseResult.getCode()) {
					result = true;
				}
			}else {
				String rechargeSn = payLog.getOrderSn();
				//更新order
				UpdateUserRechargeParam updateUserRechargeParam = new UpdateUserRechargeParam();
				updateUserRechargeParam.setPaymentCode(payLog.getPayCode());
				updateUserRechargeParam.setPaymentId(payLog.getLogId()+"");
				updateUserRechargeParam.setPaymentName(payLog.getPayName());
				updateUserRechargeParam.setPayTime(currentTime);
				updateUserRechargeParam.setStatus("1");
				updateUserRechargeParam.setRechargeSn(payLog.getOrderSn());
				BaseResult<String> baseResult = userAccountService.updateReCharege(updateUserRechargeParam);
				logger.info(loggerId + " 充值回调返回结果：status=" + baseResult.getCode()+" , message="+baseResult.getMsg());
				if(0 == baseResult.getCode()) {
					result = true;
				}
			}
			logger.info(loggerId + " 业务回调结果：result="+result);
			if(result) {
				//更新paylog状态为已支付
				PayLog updatePayLog = new PayLog();
				updatePayLog.setLogId(payLog.getLogId());
				updatePayLog.setTradeNo(rEntity.trade_no);
				updatePayLog.setIsPaid(1);
				updatePayLog.setLastTime(currentTime);
				updatePayLog.setPayTime(currentTime);
				payLogService.update(payLog);
				logger.info(loggerId + " 业务回调成功，payLog.对象状态回写结束");
				String xml = "<xml><return_code><![CDATA[SUCCESS]]></return_code> <return_msg><![CDATA[OK]]></return_msg></xml>";
				response.getWriter().write(xml);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String decodeRspSign(JSONObject jsonObject,String key) {
		String mysign = null;
		String merchant_id = jsonObject.getString("merchant_id");
		String trade_no = jsonObject.getString("trade_no");
		String order_no = jsonObject.getString("order_no");
		String total_fee = jsonObject.getString("total_fee");
		String status = jsonObject.getString("status");
		String result_code = jsonObject.getString("result_code");
		String result_msg = jsonObject.getString("result_msg");
		String notify_id = jsonObject.getString("notify_id");
		Map<String, String> map = new HashMap<String, String>();
		map.put("merchant_id", merchant_id);
		map.put("trade_no", trade_no);
		map.put("order_no", order_no);
		map.put("total_fee", total_fee);
		map.put("status", status);
		map.put("result_code", result_code);
		map.put("result_msg", result_msg);
		map.put("notify_id", notify_id);
		//将返回的参数进行验签
		mysign = Md5Utils.BuildMysign(map, key);
		return mysign;
	}
	
	private String decodeRspInfo(String data,String encryptkey) {
		//解析密文数据
		String decryData = null;
		try {
			decryData = DecipherH5.decryptData(encryptkey,data);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return decryData;
	}
	
	
}
