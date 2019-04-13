package com.dl.shop.payment.pay.yiniupay.util;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.shop.payment.pay.common.RspOrderQueryEntity;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.entity.TXScanRequestBaseEntity;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.entity.TXScanRequestCallback;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.entity.TXScanRequestCallback.TXCallback;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.entity.TXScanRequestCallback.TXSign;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.entity.TXScanRequestOrderQuery;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.entity.TXScanRequestPaidByOthers;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.entity.TXScanRequestPaidByOthersBalanceQuery;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.entity.TXScanRequestPay;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.entity.TXScanResponseBalanceQuery;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.entity.TXScanResponseOrderQuery;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.entity.TXScanResponsePay;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.enums.PayChannelEnum;
import com.dl.shop.payment.pay.tianxia.tianxiaScan.enums.TranCodeEnum;
import com.dl.shop.payment.pay.xianfeng.cash.entity.RspSingleCashEntity;
import com.dl.shop.payment.pay.yiniupay.config.YiNiuPayConfig;
import com.itrus.util.MD5Utils;

@Component
public class YiNiuScanPay {
	private final static Logger logger = LoggerFactory.getLogger(YiNiuScanPay.class);
	@Resource
	private YiNiuPayConfig ynPayConfig;

	/**
	 * 扫码支付接口
	 * 
	 * @param txScanRequestPay
	 * @param merchantStr
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public TXScanResponsePay txScanPay(TXScanRequestPay txScanRequestPay, String merchantStr) {
		String amount = txScanRequestPay.getOrderAmt();
		logger.info("请求参数为:={}", txScanRequestPay);
		if ("true".equals(ynPayConfig.getDEBUG(merchantStr))) {
			logger.info("请求金额为:={}分", amount);
			// amount = "1";
			logger.info("测试环境请求金额置为:={}分", amount);
		}
		TXScanResponsePay txScanResponsePay = new TXScanResponsePay();
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("tranCode", TranCodeEnum.PAYSCAN.getcode());
		data.put("orderAmt", amount);
		data.put("orderId", txScanRequestPay.getOrderId());
		data.put("notifyUrl", ynPayConfig.getCALLBACK_URL(merchantStr));
		data.put("goodsName", TdExpBasicFunctions.STR2HEX(txScanRequestPay.getGoodsName()));
		// String detail = txScanRequestPay.getGoodsDetail();
		// data.put("goodsDetail", TdExpBasicFunctions.STR2HEX(detail));
		data.put("stlType", txScanRequestPay.getStlType());// 结算类型
		data.put("payChannel", PayChannelEnum.CPPAY);// 支付类型 银联二维码
		data.put("termIp", txScanRequestPay.getTermIp());
		Map<String, Object> rmap = toRequestTXPay(data, merchantStr);
		Map<String, Object> _body = (Map<String, Object>) rmap.get("REP_BODY");
		Map<String, Object> _head = (Map<String, Object>) rmap.get("REP_HEAD");
		String vsign = HttpApi.getSign(_body, ynPayConfig.getMD5KEY(merchantStr));
		logger.info("获取签名:" + vsign);
		String _sign = _head.get("sign").toString();
		try {
			txScanResponsePay.setRspcode(_body.get("rspcode").toString());// 响应吗
			txScanResponsePay.setRspmsg(TdExpBasicFunctions.HEX2STR(_body.get("rspmsg").toString()));// 响应信息,16进制解密成字符串
			boolean flag = SecurityUtil.verify(vsign, _sign, ynPayConfig.getTXPUBKEY(merchantStr), true);
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
	public BaseResult<RspOrderQueryEntity> txScanOrderQuery(TXScanRequestOrderQuery txScanRequestOrderQuery, String payCode, String merchantStr) {
		RspOrderQueryEntity rspOrderQueryEntity = new RspOrderQueryEntity();
		logger.info("天下支付订单查询请求参数为:={}", txScanRequestOrderQuery);
		TXScanResponseOrderQuery txScanOrderQuery = new TXScanResponseOrderQuery();
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("tranCode", TranCodeEnum.ORDERQUERY.getcode());
		data.put("tranDate", txScanRequestOrderQuery.getTranDate());
		data.put("orderId", txScanRequestOrderQuery.getOrderId());// 订单Id和交易流水号(二选一)
		data.put("tranSeqId", txScanRequestOrderQuery.getTranSeqId());// 交易流水号和订单Id(二选一)
		Map<String, Object> rmap = toRequestTXPay(data, merchantStr);
		Map<String, Object> txPayResponseBody = (Map<String, Object>) rmap.get("REP_BODY");
		Map<String, Object> _head = (Map<String, Object>) rmap.get("REP_HEAD");
		String vsign = HttpApi.getSign(txPayResponseBody, ynPayConfig.getMD5KEY(merchantStr));
		String _sign = _head.get("sign").toString();
		if (StringUtils.isEmpty(_sign)) {
			return ResultGenerator.genFailResult("该订单不存在!!!!");
		}
		logger.info("天下支付解析签名:" + _sign);
		try {
			boolean flag = SecurityUtil.verify(vsign, _sign, ynPayConfig.getTXPUBKEY(merchantStr), true);
			logger.info("天下支付验证签名状态:", flag);
			txScanOrderQuery.setRspcode(txPayResponseBody.get("rspcode").toString());// 响应码
			if (flag) {
				if (txScanOrderQuery.isSucc()) {
					rspOrderQueryEntity.setResult_code(txPayResponseBody.get("orderState").toString());
					rspOrderQueryEntity.setPayCode(payCode);
					rspOrderQueryEntity.setType(RspOrderQueryEntity.TYPE_TIANXIA_SCAN);
					rspOrderQueryEntity.setTrade_no(txPayResponseBody.get("tranSeqId").toString());
					return ResultGenerator.genSuccessResult("succ", rspOrderQueryEntity);
				} else {
					logger.info("天下支付轮询响应信息", txScanOrderQuery.getRepCodeMsgDetail());
				}
			}
		} catch (Exception e) {
			logger.error("天下支付签名解析异常,异常信息为", e);
		}
		return ResultGenerator.genFailResult("请求天下支付回调失败");
	}

	/**
	 * 商户余额代付
	 * 
	 * @param txScanRequestPaidByOthers
	 * @return
	 */
	/*
	 * @SuppressWarnings("unchecked") public TXScanResponsePaidByOthers
	 * txScanPayFor(TXScanRequestPaidByOthers txScanRequestPaidByOthers, String
	 * merchantStr) { logger.info("请求参数={}", txScanRequestPaidByOthers); String
	 * amount = txScanRequestPaidByOthers.getTxnAmt(); if
	 * ("true".equals(ynPayConfig.getDEBUG(merchantStr))) {
	 * logger.info("请求金额为:={}分", amount); amount = "1";
	 * logger.info("测试环境请求金额置为:={}分", amount); } TXScanResponsePaidByOthers
	 * txScanResponsePaidByOthers = new TXScanResponsePaidByOthers();
	 * Map<String, Object> data = new HashMap<String, Object>();
	 * data.put("tranCode", TranCodeEnum.BALANCEPAYFOR.getcode());
	 * data.put("tranDate", txScanRequestPaidByOthers.getTranDate());
	 * data.put("orderId", txScanRequestPaidByOthers.getOrderId());
	 * data.put("txnAmt", amount); data.put("accountNo",
	 * txScanRequestPaidByOthers.getAccountNo());// 卡号 data.put("certNum",
	 * txScanRequestPaidByOthers.getCertNum());// 身份证号 data.put("bankCode",
	 * txScanRequestPaidByOthers.getBankCode());// 银行编码 data.put("bankName",
	 * TdExpBasicFunctions.STR2HEX(txScanRequestPaidByOthers.getBankName()));//
	 * 银行名称 data.put("accountName",
	 * TdExpBasicFunctions.STR2HEX(txScanRequestPaidByOthers
	 * .getAccountName()));// 持卡人 data.put("bankProv",
	 * txScanRequestPaidByOthers.getBankProv());// 开户省 data.put("bankCity",
	 * txScanRequestPaidByOthers.getBankCity());// 开户市 data.put("cnaps",
	 * txScanRequestPaidByOthers.getCnaps());// 联行号 data.put("bankBranch",
	 * TdExpBasicFunctions
	 * .STR2HEX(txScanRequestPaidByOthers.getBankBranch()));// 支行
	 * data.put("accountType", txScanRequestPaidByOthers.getAccountType());
	 * data.put("mobile", txScanRequestPaidByOthers.getMobile()); Map<String,
	 * Object> rmap = toRequestTXPay(data, merchantStr); Map<String, Object>
	 * _body = (Map<String, Object>) rmap.get("REP_BODY"); Map<String, Object>
	 * _head = (Map<String, Object>) rmap.get("REP_HEAD"); String vsign =
	 * HttpApi.getSign(_body, ynPayConfig.getMD5KEY(merchantStr)); String _sign
	 * = _head.get("sign").toString(); logger.info("解析签名:" + _sign); try {
	 * boolean flag = SecurityUtil.verify(vsign, _sign,
	 * ynPayConfig.getTXPUBKEY(merchantStr), true); logger.info("验证签名状态:" +
	 * flag);
	 * txScanResponsePaidByOthers.setRspcode(_body.get("rspcode").toString());//
	 * 响应码
	 * txScanResponsePaidByOthers.setRspmsg(TdExpBasicFunctions.HEX2STR(_body
	 * .get("rspmsg").toString()));// 响应信息,16进制解密成字符串 if (flag) {
	 * txScanResponsePaidByOthers.setOrderId(_body.get("orderId").toString());
	 * txScanResponsePaidByOthers.setSubcode(_body.get("subcode").toString());
	 * txScanResponsePaidByOthers
	 * .setSubmsg(TdExpBasicFunctions.HEX2STR(_body.get("submsg").toString()));
	 * txScanResponsePaidByOthers.setTranId(_body.get("tranId").toString()); } }
	 * catch (Exception e) { logger.error("签名解析异常,异常信息为={}", e); } return
	 * txScanResponsePaidByOthers; }
	 */
	/**
	 * 代付查询
	 * 
	 * @param txScanPayForBalanceQuery
	 * @return
	 */
	/*
	 * @SuppressWarnings({ "unchecked", "unused" }) public
	 * TXScanResponsePaidByOthersBalanceQuery
	 * payforQuery(TXScanRequestPaidByOthersBalanceQuery
	 * txScanPayForBalanceQuery, String merchantStr) { logger.info("请求参数={}",
	 * txScanPayForBalanceQuery); TXScanResponsePaidByOthersBalanceQuery
	 * txScanPayForRespBalanceQuery = new
	 * TXScanResponsePaidByOthersBalanceQuery(); Map<String, Object> data = new
	 * HashMap<String, Object>(); data.put("tranCode",
	 * TranCodeEnum.BALANCEPAYFOEQUERY.getcode()); data.put("orderId",
	 * txScanPayForBalanceQuery.getOrderId()); data.put("tranDate",
	 * txScanPayForBalanceQuery.getTranDate()); Map<String, Object> rmap =
	 * toRequestTXPay(data, merchantStr); Map<String, Object> _body =
	 * (Map<String, Object>) rmap.get("REP_BODY"); Map<String, Object> _head =
	 * (Map<String, Object>) rmap.get("REP_HEAD"); String vsign =
	 * HttpApi.getSign(_body, ynPayConfig.getMD5KEY(merchantStr)); String _sign
	 * = _head.get("sign").toString(); logger.info("解析签名:" + _sign); try {
	 * boolean flag = SecurityUtil.verify(vsign, _sign,
	 * ynPayConfig.getTXPUBKEY(merchantStr), true); logger.info("验证签名状态:" +
	 * flag);
	 * txScanPayForRespBalanceQuery.setRspcode(_body.get("rspcode").toString
	 * ());// 响应码
	 * txScanPayForRespBalanceQuery.setRspmsg(TdExpBasicFunctions.HEX2STR
	 * (_body.get("rspmsg").toString()));// 响应信息,16进制解密成字符串 if (flag) {
	 * txScanPayForRespBalanceQuery.setOrderId(_body.get("orderId").toString());
	 * txScanPayForRespBalanceQuery.setSubcode(_body.get("subcode").toString());
	 * txScanPayForRespBalanceQuery
	 * .setSubmsg(TdExpBasicFunctions.HEX2STR(_body.get("submsg").toString()));
	 * txScanPayForRespBalanceQuery.setTranId(_body.get("tranId").toString()); }
	 * } catch (Exception e) { e.printStackTrace(); } return
	 * txScanPayForRespBalanceQuery; }
	 */
	/**
	 * 查询账户余额
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	public TXScanResponseBalanceQuery queryAccount(TXScanRequestBaseEntity txScanQueryEntity, String merchantStr) {
		TXScanResponseBalanceQuery txScanBalanceQuery = new TXScanResponseBalanceQuery();
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("tranCode", TranCodeEnum.BALANCEQUERY.getcode());
		Map<String, Object> rmap = toRequestTXPay(data, merchantStr);
		logger.info("请求返回的map串:" + rmap);
		Map<String, Object> _head = (Map<String, Object>) rmap.get("REP_HEAD");
		Map<String, Object> _body = (Map<String, Object>) rmap.get("REP_BODY");
		String vsign = HttpApi.getSign(_body, ynPayConfig.getMD5KEY(merchantStr));
		String _sign = _head.get("sign").toString();
		logger.info("解析签名:" + _sign);
		try {
			boolean flag = SecurityUtil.verify(vsign, _sign, ynPayConfig.getTXPUBKEY(merchantStr), true);
			logger.info("验证签名状态:" + flag);
			txScanBalanceQuery.setRspcode(_body.get("rspcode").toString());// 响应码
			txScanBalanceQuery.setRspmsg(TdExpBasicFunctions.HEX2STR(_body.get("rspmsg").toString()));// 响应信息,16进制解密成字符串
			if (flag) {
				txScanBalanceQuery.setAcBal(_body.get("acBal").toString());
				txScanBalanceQuery.setAcT0(_body.get("acT0").toString());
				txScanBalanceQuery.setAcT1(_body.get("acT1").toString());
			}
		} catch (Exception e) {
			logger.error("天下支付签名解析异常,异常信息为", e);
		}
		return txScanBalanceQuery;
	}

	private Map<String, Object> toRequestTXPay(Map<String, Object> data, String merchantStr) {
		data.put("agtId", ynPayConfig.getAGTID(merchantStr));
		data.put("merId", ynPayConfig.getMERID(merchantStr));
		data.put("nonceStr", TdExpBasicFunctions.RANDOM(16, "0"));
		HttpApi http = new HttpApi(ynPayConfig.getREQ_URL(merchantStr), HttpApi.POST);
		Map<String, Object> hdata = new HashMap<String, Object>();
		logger.info("天下支付组装成map串加密之前={}", data);
		String sign = HttpApi.getSign(data, ynPayConfig.getMD5KEY(merchantStr));
		try {
			sign = SecurityUtil.sign(sign, ynPayConfig.getPRVKEY(merchantStr), true);
			logger.info("天下支付加密签名={}", sign);
		} catch (Exception e) {
			logger.error("天下支付签名加密异常,异常信息为", e);
		}
		hdata.put("sign", sign);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("REQ_BODY", data);
		map.put("REQ_HEAD", hdata);
		String mapStr = JUtil.toJsonString(map);
		logger.info("天下支付封装好的map串:={}", mapStr);
		logger.info("===============================请求开始===============================");
		String rdata = http.post(mapStr);
		logger.info("===============================请求结束===============================");
		Map<String, Object> rmap = JUtil.toMap(rdata);
		logger.info("天下支付请求返回的map串:" + rmap);
		return rmap;
	}
	
	
	public Boolean checkSign(TXScanRequestCallback callback, String merchantStr) {
		TXCallback txcallbackBody = callback.getREP_BODY();
		TXSign txSign = callback.getREP_HEAD();
		Map<String, Object> data = new HashMap<>();
		data.put("orderState", txcallbackBody.getOrderState());
		data.put("tranSeqId", txcallbackBody.getTranSeqId());
		data.put("orderId", txcallbackBody.getOrderId());
		data.put("payTime", txcallbackBody.getPayTime());
		data.put("orderAmt", txcallbackBody.getOrderAmt());
		String sign = HttpApi.getSign(data, ynPayConfig.getMD5KEY(merchantStr));
		boolean flag = false;
		try {
			logger.info("天下支付响应报文中的sign={}", txSign.getSign());
			logger.info("天下支付本地参数加密后的sign={}", sign);
			flag = SecurityUtil.verify(sign, txSign.getSign(), ynPayConfig.getTXPUBKEY(merchantStr), true);
			logger.info("天下支付验证签名状态:" + flag);
		} catch (Exception e) {
			logger.error("天下支付签名解析异常,异常信息为", e);
		}
		return flag;
	}

	@SuppressWarnings("unchecked")
	public RspSingleCashEntity payforQuery1(TXScanRequestPaidByOthersBalanceQuery txScanPayForBalanceQuery, String merchantStr) {
		logger.info("天下支付代付查询请求参数={}", txScanPayForBalanceQuery);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("tranCode", TranCodeEnum.BALANCEPAYFOEQUERY.getcode());
		data.put("orderId", txScanPayForBalanceQuery.getOrderId());
		data.put("tranDate", txScanPayForBalanceQuery.getTranDate());
		Map<String, Object> rmap = toRequestTXPay(data, merchantStr);
		Map<String, Object> _body = (Map<String, Object>) rmap.get("REP_BODY");
		Map<String, Object> _head = (Map<String, Object>) rmap.get("REP_HEAD");
		String vsign = HttpApi.getSign(_body, ynPayConfig.getMD5KEY(merchantStr));
		String _sign = _head.get("sign").toString();
		logger.info("天下支付代付查询解析签名:" + _sign);
		RspSingleCashEntity rspEntity = new RspSingleCashEntity();
		try {
			boolean flag = SecurityUtil.verify(vsign, _sign, ynPayConfig.getTXPUBKEY(merchantStr), true);
			logger.info("天下支付代付查询验证签名状态:" + flag);
			rspEntity.resMessage = TdExpBasicFunctions.HEX2STR(_body.get("rspmsg").toString());
			if (flag) {
				if (_body.get("subcode").toString().equals("0000")) {
					rspEntity.status = "S";
				} else if (_body.get("subcode").toString().equals("T010")) {
					rspEntity.status = "F";
				} else if (_body.get("subcode").toString().equals("T006")) {
					rspEntity.status = "I";
				}
				// txScanPayForRespBalanceQuery.setOrderId(_body.get("orderId").toString());
				// txScanPayForRespBalanceQuery.setSubcode(_body.get("subcode").toString());
				// txScanPayForRespBalanceQuery.setSubmsg(TdExpBasicFunctions.HEX2STR(_body.get("submsg").toString()));
				// txScanPayForRespBalanceQuery.setTranId(_body.get("tranId").toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("天下支付代付查询返回rspEntity:{}" + rspEntity);
		return rspEntity;
	}

	public RspSingleCashEntity txScanPayFor1(TXScanRequestPaidByOthers txScanRequestPaidByOthers, String merchantStr) {
		logger.info("天下支付代付请求参数={}", txScanRequestPaidByOthers);
		String amount = txScanRequestPaidByOthers.getTxnAmt();
		if ("true".equals(ynPayConfig.getDEBUG(merchantStr))) {
			logger.info("天下支付代付请求金额为:={}分", amount);
			amount = "300";
			logger.info("天下支付代付最低代付金额为实际金额,测试环境请求金额置为:={}分", amount);
		}
		logger.info("天下支付代付请求参数接口识别Code={}", TranCodeEnum.BALANCEPAYFOR.getcode());
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("tranCode", TranCodeEnum.BALANCEPAYFOR.getcode());
		data.put("tranDate", TdExpBasicFunctions.GETDATE());
		data.put("orderId", txScanRequestPaidByOthers.getOrderId());
		data.put("txnAmt", amount);
		data.put("accountNo", txScanRequestPaidByOthers.getAccountNo());// 卡号
		data.put("certNum", txScanRequestPaidByOthers.getCertNum());// 身份证号
		data.put("bankCode", txScanRequestPaidByOthers.getBankCode());// 银行编码
		data.put("bankName", TdExpBasicFunctions.STR2HEX(txScanRequestPaidByOthers.getBankName()));// 银行名称
		data.put("accountName", TdExpBasicFunctions.STR2HEX(txScanRequestPaidByOthers.getAccountName()));// 持卡人
		data.put("bankProv", txScanRequestPaidByOthers.getBankProv());// 开户省
		data.put("bankCity", txScanRequestPaidByOthers.getBankCity());// 开户市
		data.put("cnaps", txScanRequestPaidByOthers.getCnaps());// 联行号
		data.put("bankBranch", TdExpBasicFunctions.STR2HEX(txScanRequestPaidByOthers.getBankName()));// 支行,同银行名称
		data.put("accountType", txScanRequestPaidByOthers.getAccountType());
		data.put("mobile", txScanRequestPaidByOthers.getMobile());
		Map<String, Object> rmap = toRequestTXPay(data, merchantStr);
		Map<String, Object> _body = (Map<String, Object>) rmap.get("REP_BODY");
		Map<String, Object> _head = (Map<String, Object>) rmap.get("REP_HEAD");
		String vsign = HttpApi.getSign(_body, ynPayConfig.getMD5KEY(merchantStr));
		String _sign = _head.get("sign").toString();
		logger.info("天下支付代付解析签名:" + _sign);
		RspSingleCashEntity rspEntity = new RspSingleCashEntity();
		try {
			boolean flag = SecurityUtil.verify(vsign, _sign, ynPayConfig.getTXPUBKEY(merchantStr), true);
			logger.info("天下支付代付验证签名状态:" + flag);
			rspEntity.resMessage = TdExpBasicFunctions.HEX2STR(_body.get("rspmsg").toString());
			if (flag) {
				if (_body.get("subcode").toString().equals("0000")) {
					rspEntity.status = "S";
				} else if (_body.get("subcode").toString().equals("T010")) {
					rspEntity.status = "F";
				} else if (_body.get("subcode").toString().equals("T006")) {
					rspEntity.status = "I";
				}
				// txScanResponsePaidByOthers.setOrderId(_body.get("orderId").toString());
				// txScanResponsePaidByOthers.setSubcode(_body.get("subcode").toString());
				// txScanResponsePaidByOthers.setSubmsg(TdExpBasicFunctions.HEX2STR(_body.get("submsg").toString()));
				// txScanResponsePaidByOthers.setTranId(_body.get("tranId").toString());
			}
		} catch (Exception e) {
			logger.error("天下支付代付签名解析异常,异常信息为={}", e);
		}
		logger.info("天下支付代付=====================返回rspEntity:{}" + rspEntity);
		return rspEntity;
	}
	
	private static Map<String, Object> toTXPayFor(Map<String, Object> data) {
//		data.put("agtId", "18090197");
//		data.put("merId", "1809019760");
//		data.put("nonceStr", TdExpBasicFunctions.RANDOM(16, "0"));
//		HttpApi http = new HttpApi("http://47.75.108.4:8343/webwt/pay/gateway.do", HttpApi.POST);
		HttpApi http = new HttpApi("http://api.shuizhiying.com:9000/Order/createOrder", HttpApi.GET);
		Map<String, Object> hdata = new HashMap<String, Object>();
		logger.info("天下支付组装成map串加密之前={}", data);
		String sign = HttpApi.getSign(data, "42709e35fe4bbb6c043d265cf3ad7392");
//		try {
//			sign = SecurityUtil
//					.sign(sign,
//							"MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCtavEE/Jnp/WDgSlHIeh11FDb/VXIQeaUVWjbYHYKvWQQpOokg9l6VwhvRkxBxDFigmwWCp5dIGHSRsW702Il8QxozHEKZUS8rJntiFTnJYuHayYJL2UdIjnTvZwAjBZebIVhQbKtlqm5GnszSncxxzYK14lVrTJmg/P+lZrU7lJqcbmhAxZY+VH336SOrM4j8VoURU5aFDmQ8HYblQhW7qjT4ZYGKOISDc+uXrXlNkXYUpqqNWif31aqD5n4aT1D+EV8j616Siuoi2ySltVzW/NLmfqFC7WFX7uBuBWHIs4Ra3FML5i5jftAgsFYElLISPik5pdRk+Acf1eEdtw21AgMBAAECggEAIAPlORl1RMh9UQsHbC53L70qphrdMwNyIa4FAAL6tGqHpSDkXgx9y8IJJriyyVwRJJ1Z+BP+jtXM93S9WkB7qVAuDw+BkBJ31cyF7MakY3R4485d2qjjSxbZqbTnRo/qvD38suRNBDjhoPICOEPfkYbyQafmQyfiGOF8RMngXX404RQV61SYFiPnaSho+UvGmtr4rhR7oDEJxSj97UwVZWMJF/D7c2V9IejiUz3CG5BCHv77aCD+rjXvoP8XBzpaUXmwdxq2DS0ehTDOUUdHHWt7beLZ/9n5mB/5TquNVjpgofaJc2oimv+V6SwNo7IT0xpX9CSUm4uJst5uIqfPJQKBgQDeHNxXCGMfpOw+eWGJ9vWtBH9Vy+fs/H+8FIjjOoT7ExspxHT8YQT/IUGqDK5DCFh6PMs+nrZh0V9Ix4meoC9zYVsuXdHfCTXD7ahNB3uhQIf+3nJCxBBG1F4UsuWWK/2tUy/GxFz+5EJ8c5zmQcFSui97yVGhLt+d85ZpvvFvtwKBgQDH4C6p1GYHwQmthMEQmfe9vXOJYqMq/Vxc/pR33LcqPhQkuIo6+GOlY2xU9sySWLkmLTAyQEME3EmypfuVmuQvtjqf4gaD9gWJA0HKbzK4jBjasEwEYvoaUkZZoRSqay8E6ojoZ+AQIwr3IfqZo7pvt5CBrBHqNfyKIS3csqAV8wKBgBFuJGB0GQtHrv5sGhpwlfEA9FkwSkAPxJpm5iMm2X62jiAl+aRAnNrEqlmE6zv5cLAPC0gbvmZIvviAKC2Zln+weS5XUHv3Rg/dG6MH3kzWOpXmsQLKThiw2702GMxDIYIzkAFCMaYTFaCclgh/yoMKd2V4c/55JjVIA1rDEEFbAoGAVkUl/vmFPG2Ofs2hA0hhjQz7lF31rXWD60Oa0415pYr76MmaKE/TuqwKMzX/IF+ZMqgoeFgSVUO7r5dC2tTrDsjpzt6hD04eH4A/Yl/SlaMqiyGy/eW2q6u9X6xOHix7M/UDOMS1CmIFtpt87dmke9nQTpf1d5DrurcNc4fJ3w8CgYBnRuAElE1V92FMuYxutWpPrN6wVJc9BTfQaUs0xak05vO7MX45cdNDfz+uSnuBl0EC9PNJlPVJU9JCmVRouhb6vm/pBMJ+reakXVHngil5v+v0n7O6iIXaD1bZjJk2kGRgGSqrDhO7qnQD9xSB32peionMI6CBu6hJaz56abtYDA==",
//							true);
//			logger.info("天下支付加密签名={}", sign);
//		} catch (Exception e) {
//			logger.error("天下支付签名加密异常,异常信息为", e);
//		}
		data.put("sign", sign);
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("REQ_BODY", data);
//		map.put("REQ_HEAD", hdata);
		String mapStr = JUtil.toJsonString(data);
		logger.info("天下支付封装好的map串:={}", mapStr);
		logger.info("===============================请求开始===============================");
		String rdata="";
		try {
			rdata = http.get(mapStr,"utf-8");
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("===============================请求结束===============================");
		Map<String, Object> rmap = JUtil.toMap(rdata);
		logger.info("天下支付请求返回的map串:" + rmap);
		return rmap;
	}
	
	public static String getBackUrl() {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("partner_id", 11880031);// 商户编号
		data.put("channel_id", 9);// 渠道号
		data.put("partner_order", "2018050211256811330028");// 户订单
		data.put("order_id", "27705");// 支付订单
		data.put("user_id", "400057");// 用户id
		data.put("total_fee", 10);// 支付订单金额
		data.put("success_time", "2018-01-01");// 成功时间
		data.put("code", "1001");//状态码
		String vsign = HttpApi.getSign(data, "42709e35fe4bbb6c043d265cf3ad7392");
		String url = "http://www.baidu.com?partner_id=11880031&channel_id=9&partner_order=2018050211256811330028&"
				+ "user_id=400057&order_id=27705&total_fee=10&real_money=10&success_time=2018-01-01&code=200&sign="+vsign;
		return url;
	}
	
	public static String getNotifyUrl() {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("partner_id", 11880031);// 商户编号
		data.put("channel_id", 9);// 渠道号
		data.put("partner_order", "2018050211256811330028");// 户订单
		data.put("order_id", "27705");// 支付订单
		data.put("user_id", "400057");// 用户id
		data.put("total_fee", 10);// 支付订单金额
		data.put("success_time", "2018-01-01");// 成功时间
		data.put("code", "1001");//状态码
		String vsign = HttpApi.getSign(data, "42709e35fe4bbb6c043d265cf3ad7392");
		String url = "http://www.baidu.com?partner_id=11880031&channel_id=9&partner_order=2018050211256811330028&"
				+ "user_id=400057&order_id=27705&total_fee=10&real_money=10&success_time=2018-01-01&code=200&sign="+vsign;
		return url;
	}
	public static Integer getCurrentTimeLong() {
		LocalDateTime time = LocalDateTime.now();
		Instant instant = time.atZone(ZoneId.systemDefault()).toInstant();
		return Math.toIntExact(instant.getEpochSecond());
	}
	public static void main(String[] args) {

		String order_id = "2018050211256811330028";
		int money = 10;
		String pay_type = "wxqrcode";
		Integer time = getCurrentTimeLong();
		String mch = "119748948794";
		String sig = order_id+money+pay_type+time+mch+MD5Utils.MD5Encrpytion("42709e35fe4bbb6c043d265cf3ad7392").toLowerCase();
		String sign = MD5Utils.MD5Encrpytion(sig).toLowerCase();
		
		String url = "http://api.shuizhiying.com:9000/Order/createOrder?mch="+mch+"&money="+money+"&time="+time+ 
				"&sign="+sign+"&order_id="+order_id+"&return_url=http://www.baidu.com&notify_url=http://baidu.com&pay_type="+pay_type;
		String rdata="";
		try {
			rdata = new HttpApi(url,HttpApi.GET).get(url,"utf-8");
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
//		RspSingleCashEntity rspEntity = new RspSingleCashEntity();
//		System.out.println(TdExpBasicFunctions.HEX2STR(_body.get("rspmsg").toString()));
		logger.info("圣圆支付代付=====================返回rspEntity:{}" + rdata);
	}
}
