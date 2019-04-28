package com.dl.shop.payment.pay.rkpay.util;

import com.alibaba.fastjson.JSON;

//请求接口参数
public class RequestParams {
    public String ds_id;
    public String version="1.0";
    public long timestamp;
    public String sign_type="MD5";
    public String biz_content;
    public String sign;

    public void initParams(Config config){
        setBiz_content(config);
        timestamp=System.currentTimeMillis()/1000;
        ds_id=StaticV.ds_id;
    }

    private void setBiz_content(Config config){
        biz_content = JSON.toJSONString(config);
    }






}
