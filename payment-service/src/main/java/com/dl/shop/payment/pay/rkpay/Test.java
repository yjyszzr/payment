package com.dl.shop.payment.pay.rkpay;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import com.alibaba.druid.support.json.JSONUtils;
import com.dl.shop.payment.pay.rkpay.util.Client;
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
	/**WAP支付
	 * @return
	 */
    public String payWap(Map<String,Object> configMap){
        PayWapConfig payConfig=new PayWapConfig();
        double fee_money = Double.parseDouble(configMap.get("pay_fee").toString())+randomNum();
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
    	double fee_money = Double.parseDouble(configMap.get("pay_fee").toString())+randomNum();
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
        double fee_money = Double.parseDouble(configMap.get("pay_fee").toString())+randomNum();
//        fee_money = 0.11;
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
    /**代付
     * apply_mode=RK
	 * @return
	 */
    public String fundApply(Map<String,Object> configMap){
    	FundApplyConfig fundApplyConfig=new FundApplyConfig();
    	fundApplyConfig.initParams(staticv.getMchid(),"AS1231989424818219", "提现", "提现", "RK", "22.33", "6217000010142034811", "孙泽强",staticv.getNotify_url());
        Client client=new Client();
        String data=client.request(fundApplyConfig,"/fund/apply",staticv);
        return data;
    }
    /**代付状态查询
     * apply_mode=RK
	 * @return
	 */
    public String fundTradeQuery(Map<String,Object> configMap){
    	FundApplyConfig fundApplyConfig=new FundApplyConfig();
    	fundApplyConfig.initParams(staticv.getMchid(),"AS1231989424818219","AS1231989424818219");
        Client client=new Client();
        String data=client.request(fundApplyConfig,"/fund/tradequery",staticv);
        return data;
    }
    /**代付账户余额查询
     * apply_mode=RK
	 * @return
	 */
    public String fundAccountQuery(Map<String,Object> configMap){
    	FundApplyConfig fundApplyConfig=new FundApplyConfig();
    	fundApplyConfig.initParams(staticv.getMchid());
        Client client=new Client();
        String data=client.request(fundApplyConfig,"/fund/accountquery",staticv);
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
    public static void main(String [] args){
    	Map<String,Object> configMap = new HashMap<>();
    	configMap.put("quick_mode", "NORMAL");// 支付模式
    	configMap.put("ds_trade_no", "sfsfsdss");// 商户订单
    	configMap.put("pay_fee", "800");// 订单金额
		configMap.put("trade_subject", "sf");// 商品名称
		configMap.put("trade_memo", "sf");// 商品名称
        Test test=new Test();
//        System.out.println(test.randomNum());
//        System.out.println(test.fundApply(configMap));
        System.out.println(test.fundAccountQuery(configMap));
//        System.out.println(test.fundTradeQuery(configMap));
//        Map ms = (Map) JSONUtils.parse();
//        System.out.println(test.payQuick(configMap));
//        System.out.println(test.payWap(configMap));
//        System.out.println(test.payQrcode(configMap));
//		DecimalFormat df = new DecimalFormat("######0.00");   
//        double fee_money = Double.parseDouble("20");
//        System.out.println(df.format(fee_money)+"");
    }
}
