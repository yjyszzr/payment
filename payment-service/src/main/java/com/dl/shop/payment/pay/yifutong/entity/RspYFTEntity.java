package com.dl.shop.payment.pay.yifutong.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 0 	交易成功
	1023 签名错误
	1	交易失败
 */
public class RspYFTEntity {
	public String code;
	public String msg;
	public String success;
	public ResultYFTData data;
	
	private static Map<String,String> codeMap = new HashMap<String, String>();
	static {
		codeMap.put("0", "成功");
		codeMap.put("1", "失败，建议：根据msg中的失败信息检查");
		codeMap.put("1000", "请求方式错误，建议：检查请求方式是否为post");
		codeMap.put("1001", "接口不存在，建议：检查请求地址");
		codeMap.put("1010", "同一请求5秒内只能发起一次，建议：确保相同请求5秒内只请求一次");
		codeMap.put("1020", "请求必要参数有空值，建议：必要参数不能为空");
		codeMap.put("1021", "orderCode长度不能超过50位，建议：检查orderCode");
		codeMap.put("1022", "商户非法，建议：检查商户号是否正确");
		codeMap.put("1023", "验签失败，建议：检查参数签名是否正确");
		codeMap.put("1024", "检查参数签名是否正确，建议：检查参数签名是否正确");
		codeMap.put("2000", "商户余额不足，建议：充值余额");
		codeMap.put("2010", "订单不存在，建议：检查orderCode");
		codeMap.put("3000", "无可用设备，建议：添加设备数量；稍后再试");
		codeMap.put("3010", "设备状态异常，建议：检查手机上的app是否正常运行");
		codeMap.put("9999", "服务器内部错误，建议：联系技术人员");
		
	}
	public boolean isSucc() {
		return "0".equals(code);
	}

	public String getCodeMsgDetail() {
		return code+codeMap.get(code);
	}
	
	public class ResultYFTData{
		public String account;
		public String mchNo;
		public String orderCode;
		public String payUrl;
		public String price;
		public String realPrice;
		public String sign;
	}
}
