package com.dl.shop.payment.pay.rkpay;

import java.util.Map;
import java.util.Optional;

import org.springframework.util.StringUtils;

import com.alibaba.druid.support.json.JSONUtils;
import com.dl.shop.payment.pay.rkpay.util.Client;
import com.dl.shop.payment.pay.rkpay.util.PayConfig;
import com.dl.shop.payment.pay.rkpay.util.PayQuickConfig;
import com.dl.shop.payment.pay.rkpay.util.QueryConfig;
import com.dl.shop.payment.pay.rkpay.util.ReFundConfig;
import com.dl.shop.payment.pay.rkpay.util.ReFundQueryConfig;

public class Test {
	/**支付宝支付
	 * @return
	 */
    public String pay(){
        PayConfig payConfig=new PayConfig();
        payConfig.initParams("MC0000000000000001","DS421fefgDF","0.01","AP","test","test","notify_url","callback_url");
        Client client=new Client();
        String data=client.request(payConfig,"/pay/wap");
        return data;
    }
    
    /**银联快捷支付
     * quick_mode 支付模式NORMAL-普通模式/YT/RK/GM
	 * @return
	 */
    public String payQuick(String quick_mode){
        PayQuickConfig payQuickConfig=new PayQuickConfig();
        if("YT".equalsIgnoreCase(quick_mode)) {
        	payQuickConfig.initParams("MC0000000000000006","DS1608261827551467","0.01",
        			"test","test","notify_url","callback_url",quick_mode,"9558801001177120303","张三","141124198804215237","13888888888");
        }else if("RK".equalsIgnoreCase(quick_mode)) {
        	payQuickConfig.initParams("MC0000000000000006","DS1608261827551467","0.01",
        			"test","test","notify_url","callback_url",quick_mode,"工商银行");
        }else if("GM".equalsIgnoreCase(quick_mode)) {
        	payQuickConfig.initParams("MC0000000000000006","DS1608261827551467","0.01",
        			"test","test","notify_url","callback_url",quick_mode,"141124198804215237","张三");
        }else {
        	payQuickConfig.initParams("MC0000000000000006","DS1608261827551467","0.01",
        			"test","test","notify_url","callback_url",quick_mode);
        }
        Client client=new Client();
        String data=client.request(payQuickConfig,"/pay/quick");
        return data;
    }
    
    /**交易查询
     * @return
     */
    public String tradeQuery(){
        QueryConfig queryConfig=new QueryConfig();
        queryConfig.initParams("MC0000000000000006","AA0702000240879559","","");
        Client client=new Client();
        String data=client.request(queryConfig,"/pay/tradequery");
        return data;
    }
    
	/**发起退款
	 * 
	 * @return
	 */
    public String refund(){
        ReFundConfig refundConfig=new ReFundConfig();
        refundConfig.initParams("MC0000000000000006","AA0702000240879559","");
        Client client=new Client();
        String data=client.request(refundConfig,"/pay/refund");
        Map<String,Object> map = (Map<String,Object>)JSONUtils.parse(data);
        Optional<Map<String,Object>> omap = Optional.ofNullable(map);
        if("0".equals(omap.get().get("status"))) {//查询成功
        	refundQuery(omap.get().get("refund_no").toString());
        }
        return data;
    }
    /**退款结果查询
	 * @param refund_no交易号 由发起退款接口返回
	 * @return
	 */
    public String refundQuery(String refund_no){
    	ReFundQueryConfig reFundQueryConfig=new ReFundQueryConfig();
    	reFundQueryConfig.initParams("MC0000000000000006",refund_no);
        Client client=new Client();
        String data=client.request(reFundQueryConfig,"/pay/refundquery");
        return data;
    }
    public static void main(String [] args){
        Test test=new Test();
//        System.out.print(test.payQuick("GM"));//NORMAL-普通模式/YT/RK/GM
//        System.out.print(test.tradeQuery());
//        System.out.println(test.refund());
        System.out.println(test.refundQuery("FD0721030144500588"));
    }
}
