package com.dl.shop.payment.pay.tianxia.tianxiaScan.entity;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class TXScanResponseBaseEntity {
	/**
	 * 响应码 000000成功,其他则失败
	 */
	private String rspcode;
	/**
	 * 响应信息
	 */
	private String rspmsg;

	public boolean isSucc() {
		return "000000".equals(rspcode);
	}

	public String getRepCodeMsgDetail() {
		return rspcode + repCodeMap.get(rspcode);
	}

	private static Map<String, String> repCodeMap = new HashMap<String, String>();
	static {
		repCodeMap.put("000000", "成功");
		repCodeMap.put("100001", "订单号重复");
		repCodeMap.put("100002", "订单号不存在");
		repCodeMap.put("100003", "机构或商户号不存在");
		repCodeMap.put("100004", "参数错误");
		repCodeMap.put("100005", "签名错误");
		repCodeMap.put("100006", "支付失败");
		repCodeMap.put("100007", "支付处理中…");
		repCodeMap.put("100008", "业务暂停使用");
		repCodeMap.put("100009", "无效支付类型");
		repCodeMap.put("100010", "消息格式错误");
		repCodeMap.put("100011", "金额格式错误");
		repCodeMap.put("100012", "请重试");
		repCodeMap.put("100013", "非法手续费");
		repCodeMap.put("100014", "T0交易关闭（全渠道）");
		repCodeMap.put("100114", "T1交易关闭（全渠道）");
		repCodeMap.put("100015", "交易关闭（全渠道）");
		repCodeMap.put("100016", "无效的结算类型");
		repCodeMap.put("100017", "IP地址");
		repCodeMap.put("100020", "非法签约费率");
		repCodeMap.put("100021", "无效商户类型");
		repCodeMap.put("100022", "无效商户经营类目.");
		repCodeMap.put("100023", "未查到商户入驻信息，请确认入驻ID是否正确");
		repCodeMap.put("100024", "商户入驻信息过期");
		repCodeMap.put("100025", "报文不合法，包括格式不合法、必输请求项为空等");
		repCodeMap.put("100026", "无效业务代码");
		repCodeMap.put("100027", "组织机构代码不能为空");
		repCodeMap.put("100028", "无效的支付渠道");
		repCodeMap.put("100029", "商户入驻失败");
		repCodeMap.put("100030", "商户绑定支付渠道失败");
		repCodeMap.put("100031", "未配置渠道密钥");
		repCodeMap.put("100032", "无效商户");
		repCodeMap.put("100033", "未配置路由信息（商户路由）");
		repCodeMap.put("100034", "未配置路由信息（公共路由）");
		repCodeMap.put("100035", "商户该支付渠道已关闭");
		repCodeMap.put("100050", "商户已关闭");
		repCodeMap.put("100051", "黑名单商户");
		repCodeMap.put("100052", "商户审核拒绝");
		repCodeMap.put("100053", "商户名称已存在");
		repCodeMap.put("100054", "商户证件信息已存在");
		repCodeMap.put("100055", "商户营业执照号码已存在");
		repCodeMap.put("100056", "支付渠道不能重复绑定");
		repCodeMap.put("100057", "商户正在处理中");
		repCodeMap.put("100058", "商户等待审核中");
		repCodeMap.put("200001", "风控限制:业务暂停使用");
		repCodeMap.put("200002", "风控限制:交易最大金额限制");
		repCodeMap.put("200003", "风控限制:交易最小金额限制");
		repCodeMap.put("200004", "风控限制:交易时间段不允许");
		repCodeMap.put("200005", "风控限制:风控处理异常");
		repCodeMap.put("200006", "未找到联行号信息。");
		repCodeMap.put("200010", "商户当天交易超额");
		repCodeMap.put("200011", "该子商户号已关闭");
		repCodeMap.put("200012", "未配置子商户信息");
		repCodeMap.put("100015", "业务处理失败");
		repCodeMap.put("100019", "通用消息输出，具体详见返回内容");
		repCodeMap.put("500001", "拒绝交易");
		repCodeMap.put("400001", "系统忙,请重试");
		repCodeMap.put("600001", "账户入账失败");
		repCodeMap.put("600003", "账户出账失败");
		repCodeMap.put("600004", "账户余额不足");
	}
}
