package com.dl.shop.payment.pay.tianxia.tianxiaScan.util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dl.shop.payment.pay.tianxia.tianxiaScan.config.TXPayConfig;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.entity.TXScanRequestBaseEntity;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.entity.TXScanRequestCallback;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.entity.TXScanRequestOrderQuery;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.entity.TXScanRequestPaidByOthers;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.entity.TXScanRequestPaidByOthersBalanceQuery;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.entity.TXScanRequestPay;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.entity.TXScanResponseBalanceQuery;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.entity.TXScanResponseOrderQuery;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.entity.TXScanResponsePaidByOthers;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.entity.TXScanResponsePaidByOthersBalanceQuery;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.entity.TXScanResponsePay;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.enums.PayChannelEnum;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.enums.TranCodeEnum;

@Component
public class TXScanPay {
	private final static Logger logger = LoggerFactory.getLogger(TXScanPay.class);

	/**
	 * 扫码支付接口
	 * 
	 * @param txScanRequestPay
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public TXScanResponsePay txScanPay(TXScanRequestPay txScanRequestPay) {
		logger.info("请求参数为:={}", txScanRequestPay);
		TXScanResponsePay txScanResponsePay = new TXScanResponsePay();
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("tranCode", TranCodeEnum.PAYSCAN);
		data.put("orderAmt", txScanRequestPay.getOrderAmt());
		data.put("orderId", txScanRequestPay.getOrderId());
		data.put("notifyUrl", txScanRequestPay.getNotifyUrl());
		data.put("goodsName", TdExpBasicFunctions.STR2HEX(txScanRequestPay.getGoodsName()));
		// String detail = txScanRequestPay.getGoodsDetail();
		// data.put("goodsDetail", TdExpBasicFunctions.STR2HEX(detail));
		data.put("stlType", txScanRequestPay.getStlType());// 结算类型
		data.put("payChannel", PayChannelEnum.CPPAY);// 支付类型 银联H5
		data.put("termIp", txScanRequestPay.getTermIp());
		Map<String, Object> rmap = toRequestTXPay(data);
		Map<String, Object> _body = (Map<String, Object>) rmap.get("REP_BODY");
		Map<String, Object> _head = (Map<String, Object>) rmap.get("REP_HEAD");
		String vsign = HttpApi.getSign(_body, TXPayConfig.MD5KEY);
		logger.info("获取签名:" + vsign);
		String _sign = _head.get("sign").toString();
		try {
			txScanResponsePay.setRspcode(_body.get("rspcode").toString());// 响应吗
			txScanResponsePay.setRspmsg(TdExpBasicFunctions.HEX2STR(_body.get("rspmsg").toString()));// 响应信息,16进制解密成字符串
			boolean flag = SecurityUtil.verify(vsign, _sign, TXPayConfig.PUBKEY, true);
			logger.info("验证签名状态:" + flag);
			if (flag) {
				if (_body.containsKey("codeUrl")) {
					txScanResponsePay.setCodeUrl(_body.get("codeUrl").toString());// 二维码地址
					txScanResponsePay.setTranDate(_body.get("tranDate").toString());// 交易日期
					txScanResponsePay.setTranSeqId(_body.get("tranSeqId").toString());// 交易流水号
					txScanResponsePay.setOrderId(_body.get("orderId").toString());// 商户订单号
					txScanResponsePay.setOrderState(_body.get("orderState").toString());// 商户订单状态
					txScanResponsePay.setSign(_sign);// 签名
				}
			}
		} catch (Exception e) {
			logger.error("签名解析异常,异常信息为", e);
		}
		logger.info("接口响应解析为实体对象txScanResponsePay:={}", txScanResponsePay);
		return txScanResponsePay;
	}

	/**
	 * 订单查询接口
	 * 
	 * @param txScanRequestOrderQuery
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public TXScanResponseOrderQuery txScanOrderQuery(TXScanRequestOrderQuery txScanRequestOrderQuery) {
		logger.info("请求参数为:={}", txScanRequestOrderQuery);
		TXScanResponseOrderQuery txScanOrderQuery = new TXScanResponseOrderQuery();
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("tranCode", TranCodeEnum.ORDERQUERY);
		data.put("tranDate", txScanRequestOrderQuery);
		data.put("orderId", txScanRequestOrderQuery.getOrderId());// 订单Id和交易流水号(二选一)
		data.put("tranSeqId", txScanRequestOrderQuery.getTranSeqId());// 交易流水号和订单Id(二选一)
		Map<String, Object> rmap = toRequestTXPay(data);
		Map<String, Object> _body = (Map<String, Object>) rmap.get("REP_BODY");
		Map<String, Object> _head = (Map<String, Object>) rmap.get("REP_HEAD");
		String vsign = HttpApi.getSign(_body, TXPayConfig.MD5KEY);
		String _sign = _head.get("sign").toString();
		logger.info("解析签名:" + _sign);
		try {
			boolean flag = SecurityUtil.verify(vsign, _sign, TXPayConfig.PUBKEY, true);
			logger.info("验证签名状态:", flag);
			txScanOrderQuery.setRspcode(_body.get("rspcode").toString());// 响应码
			txScanOrderQuery.setRspmsg(TdExpBasicFunctions.HEX2STR(_body.get("rspmsg").toString()));// 响应信息,16进制解密成字符串
			if (flag) {
				txScanOrderQuery.setAgtId(_body.get("agtId").toString());// 机构号
				txScanOrderQuery.setOrderTime(_body.get("orderTime").toString());// yyyyMMddHHmmss
				txScanOrderQuery.setOrderState(_body.get("orderState").toString());// 订单状态
				txScanOrderQuery.setSettleState(_body.get("settleState").toString());// 清算状态
				txScanOrderQuery.setSettleMsg(TdExpBasicFunctions.HEX2STR(_body.get("settleMsg").toString()));// 清算信息
				txScanOrderQuery.setOrderId(_body.get("orderId").toString());// 订单Id
				txScanOrderQuery.setTranSeqId(_body.get("tranSeqId").toString());// 交易流水号
				txScanOrderQuery.setOrderAmt(_body.get("orderAmt").toString());// 订单金额
				txScanOrderQuery.setSign(_sign);
			}
		} catch (Exception e) {
			logger.error("签名解析异常,异常信息为", e);
		}
		return txScanOrderQuery;
	}

	/**
	 * 商户余额代付
	 * 
	 * @param txScanRequestPaidByOthers
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public TXScanResponsePaidByOthers txScanPayFor(TXScanRequestPaidByOthers txScanRequestPaidByOthers) {
		logger.info("请求参数={}", txScanRequestPaidByOthers);
		TXScanResponsePaidByOthers txScanResponsePaidByOthers = new TXScanResponsePaidByOthers();
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("tranCode", TranCodeEnum.BALANCEPAYFOR);
		data.put("tranDate", txScanRequestPaidByOthers.getTranDate());
		data.put("orderId", txScanRequestPaidByOthers.getOrderId());
		data.put("txnAmt", txScanRequestPaidByOthers.getTxnAmt());
		data.put("accountNo", txScanRequestPaidByOthers.getAccountNo());// 卡号
		data.put("certNum", txScanRequestPaidByOthers.getCertNum());// 身份证号
		data.put("bankCode", txScanRequestPaidByOthers.getBankCode());// 银行编码
		data.put("bankName", TdExpBasicFunctions.STR2HEX(txScanRequestPaidByOthers.getBankName()));// 银行名称
		data.put("accountName", TdExpBasicFunctions.STR2HEX(txScanRequestPaidByOthers.getAccountName()));// 持卡人
		data.put("bankProv", txScanRequestPaidByOthers.getBankProv());// 开户省
		data.put("bankCity", txScanRequestPaidByOthers.getBankCity());// 开户市
		data.put("cnaps", txScanRequestPaidByOthers.getCnaps());// 联行号
		data.put("bankBranch", TdExpBasicFunctions.STR2HEX(txScanRequestPaidByOthers.getBankBranch()));// 支行
		data.put("accountType", txScanRequestPaidByOthers.getAccountType());
		data.put("mobile", txScanRequestPaidByOthers.getMobile());
		Map<String, Object> rmap = toRequestTXPay(data);
		Map<String, Object> _body = (Map<String, Object>) rmap.get("REP_BODY");
		Map<String, Object> _head = (Map<String, Object>) rmap.get("REP_HEAD");
		String vsign = HttpApi.getSign(_body, TXPayConfig.MD5KEY);
		String _sign = _head.get("sign").toString();
		logger.info("解析签名:" + _sign);
		try {
			boolean flag = SecurityUtil.verify(vsign, _sign, TXPayConfig.PUBKEY, true);
			logger.info("验证签名状态:" + flag);
			txScanResponsePaidByOthers.setRspcode(_body.get("rspcode").toString());// 响应码
			txScanResponsePaidByOthers.setRspmsg(TdExpBasicFunctions.HEX2STR(_body.get("rspmsg").toString()));// 响应信息,16进制解密成字符串
			if (flag) {
				txScanResponsePaidByOthers.setOrderId(_body.get("orderId").toString());
				txScanResponsePaidByOthers.setSubcode(_body.get("subcode").toString());
				txScanResponsePaidByOthers.setSubmsg(TdExpBasicFunctions.HEX2STR(_body.get("submsg").toString()));
				txScanResponsePaidByOthers.setTranId(_body.get("tranId").toString());
			}
		} catch (Exception e) {
			logger.error("签名解析异常,异常信息为={}", e);
		}
		return txScanResponsePaidByOthers;
	}

	/**
	 * 代付查询
	 * 
	 * @param txScanPayForBalanceQuery
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	private TXScanResponsePaidByOthersBalanceQuery payforQuery(TXScanRequestPaidByOthersBalanceQuery txScanPayForBalanceQuery) {
		logger.info("请求参数={}", txScanPayForBalanceQuery);
		TXScanResponsePaidByOthersBalanceQuery txScanPayForRespBalanceQuery = new TXScanResponsePaidByOthersBalanceQuery();
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("tranCode", TranCodeEnum.BALANCEPAYFOEQUERY);
		data.put("orderId", txScanPayForBalanceQuery.getOrderId());
		data.put("tranDate", txScanPayForBalanceQuery.getTranDate());
		Map<String, Object> rmap = toRequestTXPay(data);
		Map<String, Object> _body = (Map<String, Object>) rmap.get("REP_BODY");
		Map<String, Object> _head = (Map<String, Object>) rmap.get("REP_HEAD");
		String vsign = HttpApi.getSign(_body, TXPayConfig.MD5KEY);
		String _sign = _head.get("sign").toString();
		logger.info("解析签名:" + _sign);
		try {
			boolean flag = SecurityUtil.verify(vsign, _sign, TXPayConfig.PUBKEY, true);
			logger.info("验证签名状态:" + flag);
			txScanPayForRespBalanceQuery.setRspcode(_body.get("rspcode").toString());// 响应码
			txScanPayForRespBalanceQuery.setRspmsg(TdExpBasicFunctions.HEX2STR(_body.get("rspmsg").toString()));// 响应信息,16进制解密成字符串
			if (flag) {
				txScanPayForRespBalanceQuery.setOrderId(_body.get("orderId").toString());
				txScanPayForRespBalanceQuery.setSubcode(_body.get("subcode").toString());
				txScanPayForRespBalanceQuery.setSubmsg(TdExpBasicFunctions.HEX2STR(_body.get("submsg").toString()));
				txScanPayForRespBalanceQuery.setTranId(_body.get("tranId").toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return txScanPayForRespBalanceQuery;
	}

	/**
	 * 查询账户余额
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	private TXScanResponseBalanceQuery queryAccount(TXScanRequestBaseEntity txScanQueryEntity) {
		TXScanResponseBalanceQuery txScanBalanceQuery = new TXScanResponseBalanceQuery();
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("tranCode", TranCodeEnum.BALANCEQUERY);
		Map<String, Object> rmap = toRequestTXPay(data);
		logger.info("请求返回的map串:" + rmap);
		Map<String, Object> _head = (Map<String, Object>) rmap.get("REP_HEAD");
		Map<String, Object> _body = (Map<String, Object>) rmap.get("REP_BODY");
		String vsign = HttpApi.getSign(_body, TXPayConfig.MD5KEY);
		String _sign = _head.get("sign").toString();
		logger.info("解析签名:" + _sign);
		try {
			boolean flag = SecurityUtil.verify(vsign, _sign, TXPayConfig.PUBKEY, true);
			logger.info("验证签名状态:" + flag);
			txScanBalanceQuery.setRspcode(_body.get("rspcode").toString());// 响应码
			txScanBalanceQuery.setRspmsg(TdExpBasicFunctions.HEX2STR(_body.get("rspmsg").toString()));// 响应信息,16进制解密成字符串
			if (flag) {
				txScanBalanceQuery.setAcBal(_body.get("acBal").toString());
				txScanBalanceQuery.setAcT0(_body.get("acT0").toString());
				txScanBalanceQuery.setAcT1(_body.get("acT1").toString());
			}
		} catch (Exception e) {
			logger.error("签名解析异常,异常信息为", e);
		}
		return txScanBalanceQuery;
	}

	private Map<String, Object> toRequestTXPay(Map<String, Object> data) {
		data.put("agtId", TXPayConfig.AGTID);
		data.put("merId", TXPayConfig.MERID);
		data.put("nonceStr", TdExpBasicFunctions.RANDOM(16, "0"));
		HttpApi http = new HttpApi(TXPayConfig.REQ_URL, HttpApi.POST);
		Map<String, Object> hdata = new HashMap<String, Object>();
		logger.info("组装成map串={}", data);
		String sign = HttpApi.getSign(data, TXPayConfig.MD5KEY);
		try {
			sign = SecurityUtil.sign(sign, TXPayConfig.PRVKEY, true);
			logger.info("加密签名={}", sign);
		} catch (Exception e) {
			logger.error("签名加密异常,异常信息为", e);
		}
		hdata.put("sign", sign);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("REQ_BODY", data);
		map.put("REQ_HEAD", hdata);
		String mapStr = JUtil.toJsonString(map);
		logger.info("封装好的map串:={}", mapStr);
		logger.info("===============================请求开始===============================");
		String rdata = http.post(mapStr);
		logger.info("===============================请求结束===============================");
		Map<String, Object> rmap = JUtil.toMap(rdata);
		logger.info("请求返回的map串:" + rmap);
		return rmap;
	}

	public Boolean checkSign(TXScanRequestCallback callback) {
		Map<String, Object> data = new HashMap<>();
		data.put("orderState", callback.getOrderState());
		data.put("tranSeqId", callback.getTranSeqId());
		data.put("orderId", callback.getOrderId());
		data.put("payTime", callback.getPayTime());
		data.put("orderAmt", callback.getOrderAmt());
		String sign = HttpApi.getSign(data, TXPayConfig.MD5KEY);
		boolean flag = false;
		try {
			logger.info("响应报文中的sign={}", callback.getSign());
			logger.info("本地参数加密后的sign={}", sign);
			flag = SecurityUtil.verify(sign, callback.getSign(), TXPayConfig.PUBKEY, true);
			logger.info("验证签名状态:" + flag);
		} catch (Exception e) {
			logger.error("签名解析异常,异常信息为", e);
		}
		return flag;
	}
}
