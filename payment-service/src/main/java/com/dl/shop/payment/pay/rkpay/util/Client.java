package com.dl.shop.payment.pay.rkpay.util;

public class Client {
    public String request(Config config,String api_fun){
        RequestParams request_params=new RequestParams();
        request_params.initParams(config);
        request_params.sign=Encrypter.sign(request_params,StaticV.secret);
        String data=HttpRequest.request(request_params,api_fun);
        return data;
    }


}
