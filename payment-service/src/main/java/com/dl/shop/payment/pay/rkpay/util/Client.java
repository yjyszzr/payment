package com.dl.shop.payment.pay.rkpay.util;

public class Client {
    public String request(Config config,String api_fun,StaticV staticv){
        RequestParams request_params=new RequestParams();
        request_params.initParams(config,staticv);
        request_params.sign=Encrypter.sign(request_params,staticv.getSecret());
        String data=HttpRequest.request(request_params,api_fun,staticv.getServerRoot());
        return data;
    }


}
