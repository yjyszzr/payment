package com.dl.shop.payment.pay.rkpay.util;

import java.lang.reflect.Field;
import java.util.HashMap;

public class Tools {
    public static HashMap<String, String> objectToMap(RequestParams config){
        HashMap<String, String> params=new HashMap<String, String>();
        if(config != null) {
            Field[] fs = config.getClass().getFields();
            if(fs != null) {
                for(int k = 0; k < fs.length; ++k) {
                    String key = fs[k].getName();
                    String value = null;
                    try {
                        Field e = fs[k];
                        Object o = e.get(config);
                        if(o != null) {
                            value = o.toString();
                        }
                    } catch (IllegalArgumentException var8) {
                        var8.printStackTrace();
                    } catch (IllegalAccessException var9) {
                        var9.printStackTrace();
                    }
                    if(value != null) {
                        params.put(key, value);
                    }
                }
            }
        }
        return params;
    }
}
