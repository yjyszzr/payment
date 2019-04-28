package com.dl.shop.payment.pay.rkpay;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.dl.shop.payment.pay.rkpay.util.Client;
import com.dl.shop.payment.pay.rkpay.util.PayConfig;
import com.dl.shop.payment.pay.rkpay.util.QueryConfig;
import com.dl.shop.payment.pay.rkpay.util.ReFundConfig;
import com.google.gson.Gson;

public class Test {
	
    public String pay(){
        PayConfig payConfig=new PayConfig();
        payConfig.initParams("MC0000000000000001","DS421fefgDF","0.01","AP","test","test","notify_url","callback_url");
        Client client=new Client();
        String data=client.request(payConfig,"/pay/wap");
        return data;
    }

    public String tradeQuery(){
        QueryConfig queryConfig=new QueryConfig();
        queryConfig.initParams("MC0000000000000001","AA0702000240879559","","");
        Client client=new Client();
        String data=client.request(queryConfig,"/pay/tradequery");
        return data;
    }

    public String refund(){
        ReFundConfig refundConfig=new ReFundConfig();
        refundConfig.initParams("MC0000000000000001","AA0702000240879559","");
        Client client=new Client();
        String data=client.request(refundConfig,"/pay/refund");
        return data;
    }

	
	public String notift(){
		InputStream is = null;
		try {
//			is = request.getInputStream();//获取输入流
			ArrayList<Byte> arr = new ArrayList<Byte>();
			byte[] buffer = new byte[50];//缓存数组
			int len;
			//读取数据
			while ((len=is.read(buffer))!=-1) {
				for (int i = 0; i < len; i++) {
					arr.add(buffer[i]);
				}
			}
			byte[] src = new byte[arr.size()];
			for (int i = 0; i < src.length; i++) {
				src[i] = arr.get(i);
			}
			String json = new String(src);
			@SuppressWarnings("unchecked")
			Map<String, Object> map = new Gson().fromJson(json, HashMap.class);
//			Context.gson.fromJson(json, HashMap.class);
			System.out.println(map);
		}  catch (Exception e) {
			e.printStackTrace();
		}  finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
    public static void main(String [] args){
        Test test=new Test();
//        System.out.print(test.tradeQuery());
        System.out.print("bbb");
//        test.tradeQuery();
    }
}
