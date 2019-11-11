package com.dl.shop.payment.pay.rkpay.util;

import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public class Encrypter {
    //MD5签名
    public static String sign(RequestParams requestParams, String secret_key){

        HashMap<String, String> params=getConfigMap(requestParams);
        Set keyset = params.keySet();
        ArrayList list = new ArrayList(keyset);
        Collections.sort(list);

        String encryptString = "";
        for(int i = 0; i < list.size(); ++i) {
            encryptString = encryptString + (String)list.get(i) + "=" + (String)params.get(list.get(i))+"&";
        }

        if(!encryptString.equals("")){
            encryptString=encryptString.substring(0,encryptString.length()-1);
        }

        return MD5(encryptString + secret_key).toUpperCase();
    }


    private static HashMap<String, String> getConfigMap(RequestParams config){
        HashMap<String, String> params=new HashMap();
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


    private static String MD5(String sourceStr) {
        String result = "";
        try {
            MessageDigest e = MessageDigest.getInstance("MD5");
            e.update(sourceStr.getBytes());
            byte[] b = e.digest();
            StringBuffer buf = new StringBuffer("");

            for(int offset = 0; offset < b.length; ++offset) {
                int i = b[offset];
                if(i < 0) {
                    i += 256;
                }
                if(i < 16) {
                    buf.append("0");
                }

                buf.append(Integer.toHexString(i));
            }

            result = buf.toString();
        } catch (NoSuchAlgorithmException var7) {
            System.out.println(var7);
        }
        return result;
    }
}
