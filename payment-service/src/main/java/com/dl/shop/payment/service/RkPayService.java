package com.dl.shop.payment.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONObject;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.pay.common.RspOrderQueryEntity;
import com.dl.shop.payment.pay.rkpay.util.Client;
import com.dl.shop.payment.pay.rkpay.util.PayQrcodeConfig;
import com.dl.shop.payment.pay.rkpay.util.PayQuickConfig;
import com.dl.shop.payment.pay.rkpay.util.PayWapConfig;
import com.dl.shop.payment.pay.rkpay.util.QueryConfig;
import com.dl.shop.payment.pay.rkpay.util.ReFundConfig;
import com.dl.shop.payment.pay.rkpay.util.ReFundQueryConfig;
import com.dl.shop.payment.web.PaymentController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RkPayService {
	private final static Logger logger = LoggerFactory.getLogger(PaymentController.class);
	
	/**WAP支付
	 * @return
	 */
    public String payWap(Map<String,Object> configMap){
        PayWapConfig payConfig=new PayWapConfig();
        double fee_money = Integer.parseInt(configMap.get("pay_fee").toString())+randomNum();
        payConfig.initParams(configMap.get("ds_trade_no").toString(),fee_money+"","AP",
        		configMap.get("trade_subject").toString(),configMap.get("trade_memo").toString());
        Client client=new Client();
        String data=client.request(payConfig,"/pay/wap");
        return data;
    }
    
    /**扫描支付(支持微信/支付宝)
	 * @return
	 */
    public String payQrcode(Map<String,Object> configMap){
    	PayQrcodeConfig payQrcodeConfig=new PayQrcodeConfig();
    	double fee_money = Integer.parseInt(configMap.get("pay_fee").toString())+randomNum();
    	payQrcodeConfig.initParams("AD1023162143432940",fee_money+"","AP","120","test","test");
        Client client=new Client();
        String data=client.request(payQrcodeConfig,"/pay/qrcode");
        return data;
    }
    
    /**网银快捷支付
     * quick_mode 支付模式NORMAL-普通模式/YT/RK/GM
	 * @return
	 */
    public String payQuick(Map<String,Object> configMap){
        PayQuickConfig payQuickConfig=new PayQuickConfig();
        double fee_money = Integer.parseInt(configMap.get("pay_fee").toString())+randomNum();
        fee_money = 0.11;
        String quick_mode = configMap.get("quick_mode")!=null?configMap.get("quick_mode").toString():"";
        if("YT".equalsIgnoreCase(quick_mode)) {
        	payQuickConfig.initParams(configMap.get("ds_trade_no").toString(),
        			fee_money+"",
        			configMap.get("trade_subject").toString(),
        			configMap.get("trade_memo").toString(),quick_mode,
        			configMap.get("account_no").toString(),
        			configMap.get("account_name").toString(),
        			configMap.get("id_no").toString(),
        			configMap.get("mobile_phone").toString());
        }else if("RK".equalsIgnoreCase(quick_mode)) {
        	payQuickConfig.initParams(configMap.get("ds_trade_no").toString(),
        			fee_money+"",
        			configMap.get("trade_subject").toString(),
        			configMap.get("trade_memo").toString(),quick_mode,
        			configMap.get("bank_name").toString());
        }else if("GM".equalsIgnoreCase(quick_mode)) {
        	payQuickConfig.initParams(configMap.get("ds_trade_no").toString(),
        			fee_money+"",
        			configMap.get("trade_subject").toString(),
        			configMap.get("trade_memo").toString(),quick_mode,
        			configMap.get("id_no").toString(),
        			configMap.get("id_name").toString());
        }else {
        	payQuickConfig.initParams(configMap.get("ds_trade_no").toString(),
        			fee_money+"",
        			configMap.get("trade_subject").toString(),
        			configMap.get("trade_memo").toString(),quick_mode);
        }
        Client client=new Client();
        String data=client.request(payQuickConfig,"/pay/quick");
        return data;
    }
    
    /**交易状态查询
     * @return
     */
    public String tradeQuery(String orderSn){
        QueryConfig queryConfig=new QueryConfig();
        queryConfig.initParams("",orderSn,"");
        Client client=new Client();
        String data=client.request(queryConfig,"/pay/tradequery");
        return data;
    }
    
	/**发起退款(暂无该接口)
	 * 
	 * @return
	 */
    public String refund(Map<String,Object> configMap){
        ReFundConfig refundConfig=new ReFundConfig();
        refundConfig.initParams("AA0702000240879559","");
        Client client=new Client();
        String data=client.request(refundConfig,"/pay/refund");
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
    	reFundQueryConfig.initParams(refund_no);
        Client client=new Client();
        String data=client.request(reFundQueryConfig,"/pay/refundquery");
        return data;
    }
    /**获取1—9的随机数
     * @return
     */
    public double randomNum() {
    	Random random = new Random();
    	int rnum = random.nextInt(8)+1;//随机1-9的正整数
    	return rnum/100d;
    }
	
	/**
	 * 支付宝H5支付
	 * @param savePayLog 支付日志
	 * @return
	 */
    public BaseResult<?> getRkPayWapUrl(PayLog savePayLog, String orderSn,String orderId,Integer userId,String paytype) throws MalformedURLException {
		BaseResult<?> payBaseResult = null;
		BigDecimal amtDouble = savePayLog.getOrderAmount();
		BigDecimal bigD = amtDouble.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_EVEN);// 金额转换成分
		Map<String,Object> param = new HashMap<>();
		param.put("ds_trade_no", orderSn);// 商户订单
		param.put("pay_fee", bigD.toString());// 订单金额
		param.put("trade_subject", paytype);// 商品名称
		param.put("trade_memo", paytype);// 商品名称
		String result = payWap(param);
		param = null;
		if (result != null && !"".equals(result)) {
			Map<String,Object> resultMap = (Map<String, Object>) JSONUtils.parse(result);
			if("0".equals(resultMap.get("status").toString())) {
				param = new HashMap<>();
				param.put("payUrl", resultMap.get("prepay_url"));
				param.put("orderId", orderId);
				param.put("payLogId", savePayLog.getLogId());
			}else {
				param = resultMap;
			}
		}
		if(param!=null) {
			payBaseResult = ResultGenerator.genSuccessResult("succ", param);
		}else {
			payBaseResult = ResultGenerator.genFailResult("Q多多支付返回数据有误");
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
		BigDecimal amtDouble = savePayLog.getOrderAmount();
		BigDecimal bigD = amtDouble.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_EVEN);// 金额转换成分
		Map<String,Object> param = new HashMap<>();
//		支付模式NORMAL-普通模式/YT/RK/GM
		//NORMAL/YT/RK/GM
		param.put("quick_mode", quick_mode);// 支付模式
		param.put("ds_trade_no", orderSn);// 商户订单
		param.put("pay_fee", bigD.toString());// 订单金额
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
		param = null;
		if (result != null && !"".equals(result)) {
			Map<String,Object> resultMap = (Map<String, Object>) JSONUtils.parse(result);
			if("0".equals(resultMap.get("status").toString())) {
				param = new HashMap<>();
				param.put("payUrl", resultMap.get("prepay_url"));
				param.put("orderId", orderId);
				param.put("payLogId", savePayLog.getLogId());
			}else {
				param = resultMap;
			}
		}
		if(param!=null) {
			payBaseResult = ResultGenerator.genSuccessResult("succ", param);
		}else {
			payBaseResult = ResultGenerator.genFailResult("Q多多网银快捷支付返回数据有误");
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
		String result = tradeQuery(orderSn);
		if(result!=null) {
			param = (Map<String, Object>) JSONUtils.parse(result);
		}
		if (param != null) {
			RspOrderQueryEntity rspOrderQueryEntity = new RspOrderQueryEntity();
			rspOrderQueryEntity.setResult_code(param.get("status")==null?"":param.get("status").toString());
			rspOrderQueryEntity.setType(RspOrderQueryEntity.TYPE_LID);
			payBaseResult = ResultGenerator.genSuccessResult("succ", rspOrderQueryEntity);
		} else {
			payBaseResult = ResultGenerator.genFailResult("Q多多支付返回数据有误");
		} 
		return payBaseResult;
	}
	
	public boolean checkMinAmount(String payToken) {
		JSONObject josn = (JSONObject) JSONObject.parse(payToken);
		BigDecimal thirdPartyPaid = new BigDecimal(josn.getString("thirdPartyPaid"));
		int paid = thirdPartyPaid.intValue();
		if(paid<100) {
			return true;
		}
		return false;
	}
	public boolean checkMaxAmount(String payToken) {
		JSONObject josn = (JSONObject) JSONObject.parse(payToken);
		BigDecimal thirdPartyPaid = new BigDecimal(josn.getString("thirdPartyPaid"));
		int paid = thirdPartyPaid.intValue();
		if(paid>3000) {
			return true;
		}
		return false;
	}
}
