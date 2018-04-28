package com.dl.shop.payment.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dl.shop.payment.pay.rongbao.config.ReapalH5Config;
import com.dl.shop.payment.pay.rongbao.entity.PayResultEntity;
import com.dl.shop.payment.pay.rongbao.util.DecipherH5;
import com.dl.shop.payment.pay.rongbao.util.Md5Utils;
import io.swagger.annotations.ApiOperation;

/***
 * 融宝callback
 * @date 2018.04.28
 */
@Controller
@RequestMapping("/rongbaopay")
public class RongbaoPlayController extends AbstractBaseController{
	private final static Logger logger = LoggerFactory.getLogger(PaymentController.class);
	
	@ApiOperation(value="融宝支付回调")
	@PostMapping("callback")
	public void payCallBackk(HttpServletRequest request, HttpServletResponse response) {
		String key = ReapalH5Config.key;
		String merchantId = request.getParameter("merchant_id");
		String data = request.getParameter("data");
		String encryptkey = request.getParameter("encryptkey");
		if(!TextUtils.isEmpty(data) && !TextUtils.isEmpty(encryptkey)) {
			logger.debug("资金方返回原key:" + encryptkey);
			logger.debug("资金方返回原数据:" + data);
			//解密返回数据
			String decryData = decodeRspInfo(data,encryptkey);
			logger.debug("数据解密结果:" + decryData);
			if(!TextUtils.isEmpty(decryData)) {
				//获取融宝支付的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
				JSONObject jsonObject = JSON.parseObject(decryData);	
				//返回sign
				String sign = jsonObject.getString("sign");
				//验签sign
				String mysign = decodeRspSign(jsonObject,key);
				logger.debug("验签:" + mysign);
				logger.debug("返回sign:" + sign);
				boolean succ = sign.equals(mysign);
				if(succ) {
					//jsonObject -> 转换业务实体类
					logger.debug("验签成功...");
					PayResultEntity rEntity = JSON.parseObject(jsonObject.toJSONString(),PayResultEntity.class);
					//更新订单信息
				}else {
					logger.debug("验签失败...");
				}
			}
		}else {
			logger.debug("资金方回调参数错误 data:" + data +" encryptkey:" + encryptkey);
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
