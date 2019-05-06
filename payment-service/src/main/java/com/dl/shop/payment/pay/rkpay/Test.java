package com.dl.shop.payment.pay.rkpay;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

import com.alibaba.druid.support.json.JSONUtils;
import com.dl.shop.payment.pay.rkpay.util.Client;
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
    public String payWap(){
        PayWapConfig payConfig=new PayWapConfig();
        double fee_money = Integer.parseInt("100")+randomNum();
        payConfig.initParams("MP1904241125194799","TEST23162143432940",fee_money+"","AP","test","test","notify_url","callback_url");
        Client client=new Client();
        String data=client.request(payConfig,"/pay/wap");
        return data;
    }
    
    /**扫描支付(支持微信/支付宝)
	 * @return
	 */
    public String payQrcode(){
    	PayQrcodeConfig payQrcodeConfig=new PayQrcodeConfig();
    	double fee_money = Integer.parseInt("100")+randomNum();
    	payQrcodeConfig.initParams("MP1904241125194799","AD1023162143432940",fee_money+"","SS","120","test","test","notify_url");
        Client client=new Client();
        String data=client.request(payQrcodeConfig,"/pay/qrcode");
        return data;
    }
    
    /**网银快捷支付
     * quick_mode 支付模式NORMAL-普通模式/YT/RK/GM
	 * @return
	 */
    public String payQuick(String quick_mode){
        PayQuickConfig payQuickConfig=new PayQuickConfig();
        if("YT".equalsIgnoreCase(quick_mode)) {
        	payQuickConfig.initParams("MC1905051517214236","TEST16082618275514","0.01",
        			"test","test","notify_url","callback_url",quick_mode,"9558801001177120303","张三","141124198804215237","13888888888");
        }else if("RK".equalsIgnoreCase(quick_mode)) {
        	payQuickConfig.initParams("MC1905051517214236","DS1608261827551467","0.01",
        			"test","test","notify_url","callback_url",quick_mode,"工商银行");
        }else if("GM".equalsIgnoreCase(quick_mode)) {
        	payQuickConfig.initParams("MC1905051517214236","DS1608261827551467","0.01",
        			"test","test","notify_url","callback_url",quick_mode,"141124198804215237","张三");
        }else {
        	payQuickConfig.initParams("MC1905051517214236","DS1608261827551467","0.01",
        			"test","test","notify_url","callback_url",quick_mode);
        }
        Client client=new Client();
        String data=client.request(payQuickConfig,"/pay/quick");
        return data;
    }
    
    /**交易状态查询
     * @return
     */
    public String tradeQuery(){
        QueryConfig queryConfig=new QueryConfig();
        queryConfig.initParams("MC1905051517214236","TEST23162143432940","","");
        Client client=new Client();
        String data=client.request(queryConfig,"/pay/tradequery");
        return data;
    }
    
	/**发起退款(暂无该接口)
	 * 
	 * @return
	 */
    public String refund(){
        ReFundConfig refundConfig=new ReFundConfig();
        refundConfig.initParams("MC1905051517214236","AA0702000240879559","");
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
    	reFundQueryConfig.initParams("MC1905051517214236",refund_no);
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
        Test test=new Test();
        System.out.println(test.payQuick("GM"));//NORMAL-普通模式/YT/RK/GM
//        System.out.println(test.tradeQuery());
//        System.out.println(test.payWap());
//        System.out.println(test.payQrcode());
    }
}
