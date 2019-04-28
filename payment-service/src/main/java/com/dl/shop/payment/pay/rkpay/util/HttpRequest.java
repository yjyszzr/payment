package com.dl.shop.payment.pay.rkpay.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class HttpRequest {

    public static String request(RequestParams data,String api_fun){
        HashMap<String, String> params=Tools.objectToMap(data);
        Set keyset = params.keySet();
        ArrayList list = new ArrayList(keyset);

        String url = StaticV.serverRoot+api_fun+"?";
        for(int i = 0; i < list.size(); ++i) {
            if(i==0) {
                url = url + (String)list.get(i) + "=" + (String)params.get(list.get(i));
            } else {
                url = url + "&" + (String)list.get(i) + "=" + (String)params.get(list.get(i));
            }
        }
        return sendGet(url);
    }

    private static String sendGet(String url) {
        String result = "";
        BufferedReader in = null;
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();

            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }
}
