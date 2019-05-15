package com.dl.shop.payment.service;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONObject;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.member.api.ISMSService;
import com.dl.member.api.IUserAccountService;
import com.dl.member.dto.SysConfigDTO;
import com.dl.member.param.SmsParam;
import com.dl.member.param.SysConfigParam;
import com.dl.shop.payment.dto.RspOrderQueryDTO;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.param.StrParam;
import com.dl.shop.payment.pay.common.RspOrderQueryEntity;
import com.dl.shop.payment.pay.rkpay.util.Client;
import com.dl.shop.payment.pay.rkpay.util.FundApplyConfig;
import com.dl.shop.payment.pay.rkpay.util.PayQrcodeConfig;
import com.dl.shop.payment.pay.rkpay.util.PayQuickConfig;
import com.dl.shop.payment.pay.rkpay.util.PayWapConfig;
import com.dl.shop.payment.pay.rkpay.util.QueryConfig;
import com.dl.shop.payment.pay.rkpay.util.ReFundConfig;
import com.dl.shop.payment.pay.rkpay.util.ReFundQueryConfig;
import com.dl.shop.payment.pay.rkpay.util.StaticV;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.entity.TXScanRequestPaidByOthers;
import com.dl.shop.payment.pay.xianfeng.cash.entity.RspSingleCashEntity;
import com.dl.shop.payment.web.PaymentController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class RkPayService {
	private final static Logger logger = LoggerFactory.getLogger(PaymentController.class);
	
	@Resource
	private StaticV staticv;
	
	@Resource
	private IUserAccountService userAccountService;
	
	@Resource
	private ISMSService smsService;
	/**WAP支付
	 * @return
	 */
    public String payWap(Map<String,Object> configMap){
        PayWapConfig payConfig=new PayWapConfig();
//        double fee_money = Double.parseDouble(configMap.get("pay_fee").toString())+randomNum();
        DecimalFormat df = new DecimalFormat("######0.00");   
		String fee_money = df.format(Double.parseDouble(configMap.get("pay_fee").toString()));
        payConfig.initParams(staticv.getMpid(),configMap.get("ds_trade_no").toString(),fee_money+"","AP",
        		configMap.get("trade_subject").toString(),configMap.get("trade_memo").toString(),
        		staticv.getNotify_url(),staticv.getCallback_url(),staticv.getExpire_time());
        Client client=new Client();
        String data=client.request(payConfig,"/pay/wap",staticv);
        return data;
    }
    
    /**扫描支付(支持微信/支付宝)
	 * @return
	 */
    public String payQrcode(Map<String,Object> configMap){
    	PayQrcodeConfig payQrcodeConfig=new PayQrcodeConfig();
//    	double fee_money = Double.parseDouble(configMap.get("pay_fee").toString())+randomNum();
    	DecimalFormat df = new DecimalFormat("######0.00");   
		String fee_money = df.format(Double.parseDouble(configMap.get("pay_fee").toString()));
    	payQrcodeConfig.initParams(staticv.getMpid(),"AD1023162143432940",fee_money+"","AP","120","test","test",staticv.getNotify_url());
        Client client=new Client();
        String data=client.request(payQrcodeConfig,"/pay/qrcode",staticv);
        return data;
    }
    
    /**网银快捷支付
     * quick_mode 支付模式NORMAL-普通模式/YT/RK/GM
	 * @return
	 */
    public String payQuick(Map<String,Object> configMap){
        PayQuickConfig payQuickConfig=new PayQuickConfig();
//        double fee_money = Double.parseDouble(configMap.get("pay_fee").toString())+randomNum();
        DecimalFormat df = new DecimalFormat("######0.00");   
		String fee_money = df.format(Double.parseDouble(configMap.get("pay_fee").toString()));
        String quick_mode = configMap.get("quick_mode")!=null?configMap.get("quick_mode").toString():"";
        if("YT".equalsIgnoreCase(quick_mode)) {
        	payQuickConfig.initParams(staticv.getMchid(),configMap.get("ds_trade_no").toString(),
        			fee_money+"",
        			configMap.get("trade_subject").toString(),
        			configMap.get("trade_memo").toString(),quick_mode,
        			configMap.get("account_no").toString(),
        			configMap.get("account_name").toString(),
        			configMap.get("id_no").toString(),
        			configMap.get("mobile_phone").toString(),
        			staticv.getNotify_url(),staticv.getCallback_url());
        }else if("RK".equalsIgnoreCase(quick_mode)) {
        	payQuickConfig.initParams(staticv.getMchid(),configMap.get("ds_trade_no").toString(),
        			fee_money+"",
        			configMap.get("trade_subject").toString(),
        			configMap.get("trade_memo").toString(),quick_mode,
        			configMap.get("bank_name").toString(),
        			staticv.getNotify_url(),staticv.getCallback_url());
        }else if("GM".equalsIgnoreCase(quick_mode)) {
        	payQuickConfig.initParams(staticv.getMchid(),configMap.get("ds_trade_no").toString(),
        			fee_money+"",
        			configMap.get("trade_subject").toString(),
        			configMap.get("trade_memo").toString(),quick_mode,
        			configMap.get("id_no").toString(),
        			configMap.get("id_name").toString(),
        			staticv.getNotify_url(),staticv.getCallback_url());
        }else {
        	payQuickConfig.initParams(staticv.getMchid(),configMap.get("ds_trade_no").toString(),
        			fee_money+"",
        			configMap.get("trade_subject").toString(),
        			configMap.get("trade_memo").toString(),quick_mode,
        			staticv.getNotify_url(),staticv.getCallback_url());
        }
        Client client=new Client();
        String data=client.request(payQuickConfig,"/pay/quick",staticv);
        return data;
    }
    /**代付账户余额查询
     * apply_mode=RK
	 * @return
	 */
    public String fundAccountQuery(){
    	FundApplyConfig fundApplyConfig=new FundApplyConfig();
    	fundApplyConfig.initParams(staticv.getMchid());
        Client client=new Client();
        String data=client.request(fundApplyConfig,"/fund/accountquery",staticv);
        return data;
    }
    /**交易状态查询
     * @return
     */
    public String tradeQuery(String orderSn){
        QueryConfig queryConfig=new QueryConfig();
        queryConfig.initParams(staticv.getMchid(),"",orderSn,"");
        Client client=new Client();
        String data=client.request(queryConfig,"/pay/tradequery",staticv);
        return data;
    }
    
	/**发起退款(暂无该接口)
	 * 
	 * @return
	 */
    public String refund(Map<String,Object> configMap){
        ReFundConfig refundConfig=new ReFundConfig();
        refundConfig.initParams(staticv.getMchid(),"AA0702000240879559","");
        Client client=new Client();
        String data=client.request(refundConfig,"/pay/refund",staticv);
        Map<String,Object> map = (Map<String,Object>)JSONUtils.parse(data);
        Optional<Map<String,Object>> omap = Optional.ofNullable(map);
        if("0".equals(omap.get().get("status"))) {//查询成功
        	refundQuery(omap.get().get("refund_no").toString());
        }
        return data;
    }
    /**退款结果查询(暂无该接口)
	 * @return String=json
	 */
    public String refundQuery(String refund_no){
    	ReFundQueryConfig reFundQueryConfig=new ReFundQueryConfig();
    	reFundQueryConfig.initParams(staticv.getMchid(),refund_no);
        Client client=new Client();
        String data=client.request(reFundQueryConfig,"/pay/refundquery",staticv);
        return data;
    }
    /**获取1—9的随机数
     * @return
     */
    public double randomNum() {
//    	Random random = new Random(); 
//    	int rnum = random.nextInt(8)+1;//随机1-9的正整数
    	return 0;
    }
	
	/**
	 * 支付宝H5支付
	 * @param savePayLog 支付日志
	 * @return
	 */
    public BaseResult<?> getRkPayWapUrl(PayLog savePayLog, String orderSn,String orderId,String paytype)  {
		BaseResult<?> payBaseResult = null;
		try {
			BigDecimal amtDouble = savePayLog.getOrderAmount();
	//		BigDecimal bigD = amtDouble.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_EVEN);// 金额转换成分
			Map<String,Object> param = new HashMap<>();
			param.put("ds_trade_no", orderSn);// 商户订单
			param.put("pay_fee", amtDouble.toString());// 订单金额
			param.put("trade_subject", paytype);// 商品名称
			param.put("trade_memo", paytype);// 商品名称
			String result = payWap(param);
			logger.info("Q多多返回结果："+result);
			param = null;
			if (result != null && !"".equals(result)) {
				Map<String,Object> resultMap = (Map<String, Object>) JSONUtils.parse(result);
				if("0".equals(resultMap.get("status").toString())) {
					param = new HashMap<>();
					param.put("payUrl", resultMap.get("prepay_url"));
					param.put("orderId", orderId);
					param.put("payLogId", savePayLog.getLogId());
				}
			}
			if(param!=null) {
				payBaseResult = ResultGenerator.genSuccessResult("succ", param);
			}else {
				payBaseResult = ResultGenerator.genFailResult("WAP支付返回数据有误");
			}
		} catch (Exception e) {
			log.info("WAP支付返回数据错误");
			payBaseResult = ResultGenerator.genFailResult("接口内部错误");
		}
		return payBaseResult;
	}
	/**
	 * 网银快捷支付payQuick app_rkquick
	 * @param savePayLog 支付日志
	 * @return
	 */
	public BaseResult<?> getRkPayQuickUrl(PayLog savePayLog,String quick_mode,String orderSn,String orderId,String paytype,String id_no,String mobile_phone,
			String bank_name,String user_name,String account_no) {
		BaseResult<?> payBaseResult = null;
		try {
			BigDecimal amtDouble = savePayLog.getOrderAmount();
	//		BigDecimal bigD = amtDouble.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_EVEN);// 金额转换成分
			Map<String,Object> param = new HashMap<>();
	//		支付模式NORMAL-普通模式/YT/RK/GM
			//NORMAL/YT/RK/GM
			param.put("quick_mode", quick_mode);// 支付模式
			param.put("ds_trade_no", orderSn);// 商户订单
			param.put("pay_fee", amtDouble.toString());// 订单金额
			param.put("trade_subject", paytype);// 商品名称
			param.put("trade_memo", paytype);// 商品名称
			if("YT".equalsIgnoreCase(quick_mode)) {//YT
				param.put("account_no", account_no);
				param.put("account_name", user_name);
				param.put("id_no", id_no);
				param.put("mobile_phone", mobile_phone);
			} else if("RK".equalsIgnoreCase(quick_mode)) {	//RK
				param.put("bank_name", bank_name);
			} else if("GM".equalsIgnoreCase(quick_mode)) {	//GM
				param.put("id_no", id_no);
				param.put("id_name", user_name);
			}
			String result = payQuick(param);
	//		logger.info("Q多多参数值："+JSONUtils.toJSONString(staticv));
			logger.info("Q多多返回结果："+result+"参数："+staticv.getDs_id());
			param = null;
			if (result != null && !"".equals(result)) {
				Map<String,Object> resultMap = (Map<String, Object>) JSONUtils.parse(result);
				if("0".equals(resultMap.get("status").toString())) {
					param = new HashMap<>();
					param.put("payUrl", resultMap.get("prepay_url"));
					param.put("orderId", orderId);
					param.put("payLogId", savePayLog.getLogId());
				}
			}
			if(param!=null) {
				payBaseResult = ResultGenerator.genSuccessResult("succ", param);
			}else {
				payBaseResult = ResultGenerator.genFailResult("网银快捷支付返回数据有误");
			}
		}catch (Exception e) {
			log.info("网银快捷支付返回数据有误");
			payBaseResult = ResultGenerator.genFailResult("接口内部错误");
		}
		return payBaseResult;
	}
	
	/**
	 * 网银快捷支付payQuick app_rkquick  财务专用商户充值
	 * @param savePayLog 支付日志
	 * @return
	 */
	public BaseResult<RspOrderQueryDTO> getShMoney(StrParam emptyParam ) {
		BaseResult<RspOrderQueryDTO> payBaseResult = null;
		try {
			String result = fundAccountQuery();
			logger.info("Q多多返回结果："+result+"参数："+staticv.getDs_id());
			Map<String,Object> param = null;
			RspOrderQueryDTO ymoneyDTO = new RspOrderQueryDTO();
			if (result != null && !"".equals(result)) {
				Map<String,Object> resultMap = (Map<String, Object>) JSONUtils.parse(result);
				if("0".equals(resultMap.get("status").toString())) {
					param = resultMap;
					ymoneyDTO.setCode(0);
					ymoneyDTO.setMsg(resultMap.get("message").toString());
					ymoneyDTO.setDonationPrice(resultMap.get("account_balance").toString());
				}else {
					ymoneyDTO.setCode(1);
					ymoneyDTO.setMsg(resultMap.get("message").toString());
				}
			}

			if(param!=null) {
				payBaseResult = ResultGenerator.genSuccessResult("succ", ymoneyDTO);
			}else {
				payBaseResult = ResultGenerator.genFailResult("商户余额查询失败");
			}
		}catch (Exception e) {
			log.info("网银快捷支付返回数据有误");
			payBaseResult = ResultGenerator.genFailResult("接口内部错误");
		}
		return payBaseResult;
	}
	/**
	 * 查询订单状态
	 * @param orderSn
	 * @return
	 */
	public BaseResult<RspOrderQueryEntity> commonOrderQueryLid(String orderSn) throws MalformedURLException{
		BaseResult<RspOrderQueryEntity> payBaseResult = null;
		Map<String,Object> param = null;
		try {
			String result = tradeQuery(orderSn);
			if(result!=null) {
				param = (Map<String, Object>) JSONUtils.parse(result);
			}
			if (param != null) {
				RspOrderQueryEntity rspOrderQueryEntity = new RspOrderQueryEntity();
				rspOrderQueryEntity.setResult_code(param.get("status")==null?"":param.get("status").toString());
				rspOrderQueryEntity.setType(RspOrderQueryEntity.TYPE_RKPAY);
				payBaseResult = ResultGenerator.genSuccessResult("succ", rspOrderQueryEntity);
			} else {
				payBaseResult = ResultGenerator.genFailResult("查询返回数据有误");
			} 
		} catch (Exception e) {
			log.info("订单状态查询返回数据错误");
			payBaseResult = ResultGenerator.genFailResult("接口内部错误");
		}
		return payBaseResult;
	}
	
	
	 /**代付
     * apply_mode=RK
	 * @return
	 */
    public RspSingleCashEntity fundApply(TXScanRequestPaidByOthers txScanRequestPaidByOthers){
    	FundApplyConfig fundApplyConfig=new FundApplyConfig();
//    	double fee_money = Double.parseDouble(configMap.get("trade_fee").toString())+randomNum();
        RspSingleCashEntity rspEntity = new RspSingleCashEntity();
        try {
        	logger.info("Q多多代付请求参数={}", txScanRequestPaidByOthers);
    		String amount = txScanRequestPaidByOthers.getTxnAmt()!=null?txScanRequestPaidByOthers.getTxnAmt():"0";
    		logger.info("Q多多代付请求金额为:={}元", Double.parseDouble(amount)/100);
    		DecimalFormat df = new DecimalFormat("######0.00");   
    		String trade_fee = df.format(Double.parseDouble(amount)/100);
    		logger.info("Q多多代付请求金额为:={}元", trade_fee);
        	fundApplyConfig.initParams(staticv.getMchid(),txScanRequestPaidByOthers.getOrderId(), 
        			"提现","提现","RK",trade_fee,txScanRequestPaidByOthers.getAccountNo(),
        			txScanRequestPaidByOthers.getAccountName(),staticv.getFund_notify_url());
        	Client client=new Client();
        	String data=client.request(fundApplyConfig,"/fund/apply",staticv);
            Map<String,Object> resultMap = (Map<String, Object>) JSONUtils.parse(data);
            rspEntity.resMessage = resultMap.get("message")!=null?resultMap.get("message").toString():"";
            String status = resultMap.get("status")!=null?resultMap.get("status").toString():"";
            if("0".equals(status)) {//接口成功，并不是提现成功
            	rspEntity.status = "S";
            	//低于指定数额发短信通知
            	String strMoney = "0";
            	BaseResult<RspOrderQueryDTO> ymoney = getShMoney(null);
    			if(ymoney!=null && ymoney.getData()!=null) {
    				strMoney=ymoney.getData().getDonationPrice()!=null?ymoney.getData().getDonationPrice():"0";//商户余额
    			}
    			SysConfigParam cfg = new SysConfigParam();
    			cfg.setBusinessId(66);//读取最低商户余额短信通知指标
    			SysConfigDTO sysConfigDTO = userAccountService.queryBusinessLimit(cfg)!=null?userAccountService.queryBusinessLimit(cfg).getData():new SysConfigDTO();
    			int totalYmoney = sysConfigDTO.getValue()!=null?sysConfigDTO.getValue().intValue():0;
    			String mobile = sysConfigDTO.getValueTxt();
    			if(totalYmoney>Double.parseDouble(strMoney)) {//发送短信
    				SmsParam smsParam = new SmsParam();
    				smsParam.setSmsType("4");
    				smsParam.setMobile(mobile!=null?mobile:"");
    				log.info("进入发送短信环节：接受短信手机号："+mobile);
    				smsService.sendSmsCode(smsParam);
    			}
    			
            } else {//接口失败
            	rspEntity.status = "F";
            }
        }catch (Exception e) {
			log.info("代付返回数据错误");
			rspEntity.resMessage = "接口内部错误";
            rspEntity.status = "F";
		}
        return rspEntity;
    }
	
	public boolean checkMinAmount(String payToken,String payCode) {
		JSONObject josn = (JSONObject) JSONObject.parse(payToken);
		BigDecimal thirdPartyPaid = new BigDecimal(josn.getString("thirdPartyPaid"));
		int paid = thirdPartyPaid.intValue();
		if("app_rkwap".equals(payCode)) {
			if(paid<80) {
				return true;
			}
		}
		if(paid<20) {
			return true;
		}
		return false;
	}
	public boolean checkMaxAmount(String payToken,String payCode) {
		JSONObject josn = (JSONObject) JSONObject.parse(payToken);
		BigDecimal thirdPartyPaid = new BigDecimal(josn.getString("thirdPartyPaid"));
		int paid = thirdPartyPaid.intValue();
		if("app_rkwap".equals(payCode)) {
			if(paid>3000) {
				return true;
			}
		}
		if(paid>5000) {
			return true;
		}
		return false;
	}
}
