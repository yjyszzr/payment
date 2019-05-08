package com.dl.shop.payment.pay.rongbao.util;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSON;
import com.dl.shop.payment.pay.rongbao.config.ReapalH5Config;

/* *
 *类名：ReapalSubmit
 *功能：融宝各接口请求提交类
 *详细：构造融宝各接口发送请求数据，获取远程HTTP数据
 *版本：3.1.2
 *日期：2015-08-15
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究融宝接口使用，只是提供一个参考。
 */
@Component
public class ReapalSubmit {

	@Resource
	private ReapalH5Config rongCfg;
	@Resource
	private DecipherH5 decipherH5;
	
	/**
     * 发送请求
     * @return 返回结果
     */
	public String buildSubmit(Map<String, String> para, String url) throws Exception {
        //生成签名
		String mysign = Md5Utils.BuildMysign(para,rongCfg.getKey());
        para.put("sign", mysign);
        //将map转化为json 串
		String json = JSON.toJSONString(para);
		//加密数据
		Map<String, String> maps = decipherH5.encryptData(json);
		maps.put("merchant_id", rongCfg.getMerchant_id());
		//发送请求 并接收
		String post = HttpClientUtil.post(rongCfg.getRongpay_api()+ url, maps);
		return post;
	}
	
	
}
