package com.dl.shop.payment.pay.rkpay;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import com.alibaba.druid.support.json.JSONUtils;
import com.dl.base.result.BaseResult;
import com.dl.member.dto.SysConfigDTO;
import com.dl.member.param.SmsParam;
import com.dl.member.param.SysConfigParam;
import com.dl.shop.payment.dto.RspOrderQueryDTO;
import com.dl.shop.payment.pay.rkpay.util.Client;
import com.dl.shop.payment.pay.rkpay.util.Config;
import com.dl.shop.payment.pay.rkpay.util.FundApplyConfig;
import com.dl.shop.payment.pay.rkpay.util.PayQrcodeConfig;
import com.dl.shop.payment.pay.rkpay.util.PayQuickConfig;
import com.dl.shop.payment.pay.rkpay.util.PayWapConfig;
import com.dl.shop.payment.pay.rkpay.util.QueryConfig;
import com.dl.shop.payment.pay.rkpay.util.ReFundConfig;
import com.dl.shop.payment.pay.rkpay.util.ReFundQueryConfig;
import com.dl.shop.payment.pay.rkpay.util.StaticV;

public class Test {

	private StaticV staticv = new StaticV();

	/**
	 * WAP支付
	 * 
	 * @return
	 */
	public String payWap(Map<String, Object> configMap) {
		PayWapConfig payConfig = new PayWapConfig();
		double fee_money = Double.parseDouble(configMap.get("pay_fee").toString()) + randomNum();
		payConfig.initParams(staticv.getMpid(), configMap.get("ds_trade_no").toString(), fee_money + "", "AP",
				configMap.get("trade_subject").toString(), configMap.get("trade_memo").toString(),
				staticv.getNotify_url(), staticv.getCallback_url(), staticv.getExpire_time());
		Client client = new Client();
		String data = client.request(payConfig, "/pay/wap", staticv);
		return data;
	}

	/**
	 * 扫描支付(支持微信/支付宝)
	 * 
	 * @return
	 */
	public String payQrcode(Map<String, Object> configMap) {
		PayQrcodeConfig payQrcodeConfig = new PayQrcodeConfig();
		double fee_money = Double.parseDouble(configMap.get("pay_fee").toString()) + randomNum();
		payQrcodeConfig.initParams(staticv.getMpid(), "AD1023162143432940", fee_money + "", "AP", "120", "test", "test",
				staticv.getNotify_url());
		Client client = new Client();
		String data = client.request(payQrcodeConfig, "/pay/qrcode", staticv);
		return data;
	}

	/**
	 * 网银快捷支付 quick_mode 支付模式NORMAL-普通模式/YT/RK/GM
	 * 
	 * @return
	 */
	public String payQuick(Map<String, Object> configMap) {
		PayQuickConfig payQuickConfig = new PayQuickConfig();
		double fee_money = Double.parseDouble(configMap.get("pay_fee").toString()) + randomNum();
//        fee_money = 0.11;
		String quick_mode = configMap.get("quick_mode") != null ? configMap.get("quick_mode").toString() : "";
		if ("YT".equalsIgnoreCase(quick_mode)) {
			payQuickConfig.initParams(staticv.getMchid(), configMap.get("ds_trade_no").toString(), fee_money + "",
					configMap.get("trade_subject").toString(), configMap.get("trade_memo").toString(), quick_mode,
					configMap.get("account_no").toString(), configMap.get("account_name").toString(),
					configMap.get("id_no").toString(), configMap.get("mobile_phone").toString(),
					staticv.getNotify_url(), staticv.getCallback_url());
		} else if ("RK".equalsIgnoreCase(quick_mode)) {
			payQuickConfig.initParams(staticv.getMchid(), configMap.get("ds_trade_no").toString(), fee_money + "",
					configMap.get("trade_subject").toString(), configMap.get("trade_memo").toString(), quick_mode,
					configMap.get("bank_name").toString(), staticv.getNotify_url(), staticv.getCallback_url());
		} else if ("GM".equalsIgnoreCase(quick_mode)) {
			payQuickConfig.initParams(staticv.getMchid(), configMap.get("ds_trade_no").toString(), fee_money + "",
					configMap.get("trade_subject").toString(), configMap.get("trade_memo").toString(), quick_mode,
					configMap.get("id_no").toString(), configMap.get("id_name").toString(), staticv.getNotify_url(),
					staticv.getCallback_url());
		} else {
			payQuickConfig.initParams(staticv.getMchid(), configMap.get("ds_trade_no").toString(), fee_money + "",
					configMap.get("trade_subject").toString(), configMap.get("trade_memo").toString(), quick_mode,
					staticv.getNotify_url(), staticv.getCallback_url());
		}
		Client client = new Client();
		String data = client.request(payQuickConfig, "/pay/quick", staticv);
		return data;
	}

	/**
	 * 交易状态查询
	 * 
	 * @return
	 */
	public String tradeQuery(String orderSn) {
		QueryConfig queryConfig = new QueryConfig();
		queryConfig.initParams(staticv.getMchid(), "", orderSn, "");
		Client client = new Client();
		String data = client.request(queryConfig, "/pay/tradequery", staticv);
		return data;
	}

	/**
	 * 发起退款(暂无该接口)
	 * 
	 * @return
	 */
	public String refund(Map<String, Object> configMap) {
		ReFundConfig refundConfig = new ReFundConfig();
		refundConfig.initParams(staticv.getMchid(), "AA0702000240879559", "");
		Client client = new Client();
		String data = client.request(refundConfig, "/pay/refund", staticv);
		Map<String, Object> map = (Map<String, Object>) JSONUtils.parse(data);
		Optional<Map<String, Object>> omap = Optional.ofNullable(map);
		if ("0".equals(omap.get().get("status"))) {// 查询成功
			refundQuery(omap.get().get("refund_no").toString());
		}
		return data;
	}

	/**
	 * 退款结果查询(暂无该接口)
	 * 
	 * @return String=json
	 */
	public String refundQuery(String refund_no) {
		ReFundQueryConfig reFundQueryConfig = new ReFundQueryConfig();
		reFundQueryConfig.initParams(staticv.getMchid(), refund_no);
		Client client = new Client();
		String data = client.request(reFundQueryConfig, "/pay/refundquery", staticv);
		return data;
	}

	/**
	 * 代付 apply_mode=RK
	 * 
	 * @return
	 */
	public String fundApply(Map<String, Object> configMap) {
		FundApplyConfig fundApplyConfig = new FundApplyConfig();
		fundApplyConfig.initParams(staticv.getMchid(), "AS1231989424818219", "提现", "提现", "RK", "124.90",
				"6217000010142034811", "孙泽强", staticv.getNotify_url());
		Client client = new Client();
		String data = client.request(fundApplyConfig, "/fund/apply", staticv);
		return data;
	}

	/**
	 * 代付状态查询 apply_mode=RK
	 * 
	 * @return
	 */
	public String fundTradeQuery(Map<String, Object> configMap) {
		FundApplyConfig fundApplyConfig = new FundApplyConfig();
		fundApplyConfig.initParams(staticv.getMchid(), "", "2019052221181328290789");
		Client client = new Client();
		String data = client.request(fundApplyConfig, "/fund/tradequery", staticv);
		return data;
	}

	/**
	 * 代付账户余额查询 apply_mode=RK
	 * 
	 * @return
	 */
	public String fundAccountQuery(Map<String, Object> configMap) {
		FundApplyConfig fundApplyConfig = new FundApplyConfig();
		fundApplyConfig.initParams(staticv.getMchid());
		Client client = new Client();
		String data = client.request(fundApplyConfig, "/fund/accountquery", staticv);
		return data;
	}

	/**
	 * 获取1—9的随机数
	 * 
	 * @return
	 */
	public int randomNum() {
		Random random = new Random();
		int rnum = random.nextInt(5) + 1;// 随机1-9的正整数
		if(rnum==2) {
			return randomNum();
		}
		return rnum;
	}

	public static void main(String[] args) {
		Map<String, Object> configMap = new HashMap<>();
		configMap.put("quick_mode", "NORMAL");// 支付模式
		configMap.put("ds_trade_no", "sfsfsdss");// 商户订单
		configMap.put("pay_fee", "800");// 订单金额
		configMap.put("trade_subject", "sf");// 商品名称
		configMap.put("trade_memo", "sf");// 商品名称
		Test test = new Test();
		for (int i = 0; i < 10; i++) {
			System.out.println(test.randomNum());
		}
//        System.out.println(test.randomNum());
//        System.out.println(test.fundApply(configMap));
//        System.out.println(test.fundAccountQuery(configMap));
//        System.out.println(test.fundTradeQuery(configMap));
//        String funddata = test.fundTradeQuery(configMap);///查询代付状态
//        System.out.println(funddata);
//    	Map<String,Object> funddataMap = (Map<String, Object>) JSONUtils.parse(funddata);
//    	if(funddataMap!=null && "0".equals(funddataMap.get("status").toString())) {//代付状态查询成功  判断代付是否成功
//    		if(funddataMap.get("trade_status").toString().equals("FAIL")) {//代付失败
//    			System.out.println(false);
//    		}else {//代付成功
//    			System.out.println(true);
//    		}
//    	}else { ///代付查询失败
//    		System.out.println(false);
//    	}
//        Map ms = (Map) JSONUtils.parse();
//        System.out.println(test.payQuick(configMap));
//        System.out.println(test.payWap(configMap));
//        System.out.println(test.payQrcode(configMap));
//		DecimalFormat df = new DecimalFormat("######0.00");   
//        double fee_money = Double.parseDouble("20");
//        System.out.println(df.format(fee_money)+"");
//		int s = 5000;
//		double m = 5544.55;
//		System.out.println(s<m);

//        HashMap<String,Object> rmap = new HashMap();
//        System.out.println(rmap);

		List<Integer> intList = new ArrayList<>();
		intList.add(1);
		intList.add(3);
		intList.add(4);
		intList.add(2);

//        intList.stream().sorted(Comparator.comparingInt(Integer::intValue).reversed())
//		intList.stream().filter(dto -> {
//			if (dto.intValue() == 4) {
//				return false;
//			}
//			return true;
//		}).forEach(dto -> {
//
//			System.out.println(dto);
//		});
		
//        System.out.println(intList.stream().filter(dto -> {
//			if (dto.intValue() == 4) {
//				return false;
//			}
//			return true;
//		}).min(Comparator.reverseOrder()).get());
//		System.out.println("1.3.0".compareTo("1.1.0"));
//		System.out.println("1.3.0".compareTo("1.2.9"));
//		System.out.println("1.3.0".compareTo("1.3.0"));
	}
}
