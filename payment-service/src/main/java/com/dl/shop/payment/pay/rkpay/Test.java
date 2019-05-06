package com.dl.shop.payment.pay.rkpay;

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

public class Test {
	/**WAP支付
	 * @return
	 */
    public String payWap(Map<String,Object> configMap){
        PayWapConfig payConfig=new PayWapConfig();
        double fee_money = Integer.parseInt("100")+randomNum();
        payConfig.initParams("TEST23162143432940",fee_money+"","AP","test","test");
        Client client=new Client();
        String data=client.request(payConfig,"/pay/wap");
        return data;
    }
    
    /**扫描支付(支持微信/支付宝)（暂无接口）
	 * @return
	 */
    public String payQrcode(Map<String,Object> configMap){
    	PayQrcodeConfig payQrcodeConfig=new PayQrcodeConfig();
    	double fee_money = Integer.parseInt("100")+randomNum();
    	payQrcodeConfig.initParams("AD1023162143432940",fee_money+"","AP","120","test","test");
        Client client=new Client();
        String data=client.request(payQrcodeConfig,"/pay/qrcode");
        return data;
    }
    
    /**网银快捷支付
     * quick_mode 支付模式NORMAL-普通模式/YT/RK/GM
	 * @return
	 */
    public String payQuick(String quick_mode,Map<String,Object> configMap){
        PayQuickConfig payQuickConfig=new PayQuickConfig();
        if("YT".equalsIgnoreCase(quick_mode)) {
        	payQuickConfig.initParams("TEST16082618275514","0.01","test","test",quick_mode,"9558801001177120303","张三","141124198804215237","13888888888");
        }else if("RK".equalsIgnoreCase(quick_mode)) {
        	payQuickConfig.initParams("DS1608261827551467","0.01","test","test",quick_mode,"工商银行");
        }else if("GM".equalsIgnoreCase(quick_mode)) {
        	payQuickConfig.initParams("DS1608261827551467","0.01","test","test",quick_mode,"141124198804215237","张三");
        }else {
        	payQuickConfig.initParams("DS1608261827551467","0.01","test","test",quick_mode);
        }
        Client client=new Client();
        String data=client.request(payQuickConfig,"/pay/quick");
        return data;
    }
    /**代付
     * apply_mode=RK
	 * @return
	 */
    public String fundApply(Map<String,Object> configMap){
    	FundApplyConfig fundApplyConfig=new FundApplyConfig();
    	fundApplyConfig.initParams("AS1231989424818219", "提现", "提现", "RK", "22.33", "6217000010142034811", "孙泽强");
        Client client=new Client();
        String data=client.request(fundApplyConfig,"/fund/apply");
        return data;
    }
    /**代付状态查询
     * apply_mode=RK
	 * @return
	 */
    public String fundTradeQuery(Map<String,Object> configMap){
    	FundApplyConfig fundApplyConfig=new FundApplyConfig();
    	fundApplyConfig.initParams("AS1231989424818219","AS1231989424818219");
        Client client=new Client();
        String data=client.request(fundApplyConfig,"/fund/tradequery");
        return data;
    }
    /**代付账户余额查询
     * apply_mode=RK
	 * @return
	 */
    public String fundAccountQuery(Map<String,Object> configMap){
    	FundApplyConfig fundApplyConfig=new FundApplyConfig();
    	fundApplyConfig.initParams();
        Client client=new Client();
        String data=client.request(fundApplyConfig,"/fund/accountquery");
        return data;
    }
    /**交易状态查询
     * @return
     */
    public String tradeQuery(Map<String,Object> configMap){
        QueryConfig queryConfig=new QueryConfig();
        queryConfig.initParams("","TEST23162143432940","");
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
    public static void main(String [] args){
    	Map<String,Object> configMap = new HashMap<>();
        Test test=new Test();
//        System.out.println(test.randomNum());
//        System.out.println(test.fundApply(configMap));
//        System.out.println(test.fundAccountQuery(configMap));
//        System.out.println(test.fundTradeQuery(configMap));
//        Map ms = (Map) JSONUtils.parse();
        System.out.println(test.payQuick("NORMAL",configMap));
        System.out.println(test.payWap(configMap));
        System.out.println(test.payQrcode(configMap));
    }
}
