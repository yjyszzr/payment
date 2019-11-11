/**
 * 项目名称(中文)
 * 项目名称(英文)
 * Copyright (c) 2016 ChinaPay Ltd. All Rights Reserved.
 */
package com.dl.shop.payment.pay.chinapay.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import com.dl.shop.payment.pay.chinapay.common.Constants;

/**
 * @author hrtc .
 */
public class HttpUtils {

    /**
     * 超时时间 .
     */
    private static int SOCKET_TIMEOUT = 60000;
    /**
     * 连接超时时间 .
     */
    private static int CONNECT_TIMEOUT = 60000;

    /**
     * 发送http,注意如果调用频繁请自行改造成http连接池.
     * 
     * @param url
     *            请求地址
     * @param dataMap
     *            请求map
     * @return 返回字符串
     */
    public static String send(String url, Map<String, String> dataMap) {
        CloseableHttpClient client = null;
        CloseableHttpResponse resp = null;
        try {
            client = HttpClients.custom().build();
            RequestConfig config = RequestConfig.custom()
                    .setSocketTimeout(SOCKET_TIMEOUT)
                    .setConnectTimeout(CONNECT_TIMEOUT)
                    .setAuthenticationEnabled(false).build();

            HttpPost post = new HttpPost(url);
            post.setProtocolVersion(org.apache.http.HttpVersion.HTTP_1_1);
            post.setConfig(config);

            HttpEntity entity = null;
            List<NameValuePair> formpair = new ArrayList<NameValuePair>();
            {
                for (String str : dataMap.keySet().toArray(
                        new String[dataMap.size()])) {
                    formpair.add(new BasicNameValuePair(str, dataMap.get(str)
                            .toString()));
                }
            }

            entity = new UrlEncodedFormEntity(formpair, Constants.ENCODING);
            if (entity != null) {
                post.setEntity(entity);
            }
            resp = client.execute(post);
            if (resp.getStatusLine().getStatusCode() == 200) {
                InputStream is = null;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    byte[] buffer = new byte[1024];
                    is = resp.getEntity().getContent();
                    int count = is.read(buffer);
                    while (count != -1) {
                        baos.write(buffer, 0, count);
                        count = is.read(buffer);
                    }
                    return baos.toString(Constants.ENCODING);
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } finally {
                            // ignore
                        }
                    }
                    if (baos != null) {
                        try {
                            baos.close();
                        } finally {
                            // ignore
                        }
                    }
                }
            } else {
                throw new RuntimeException(String.format(
                        "发送请求失败,statusCode=%s", resp.getStatusLine()
                                .getStatusCode()));
            }

        } catch (ClientProtocolException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (resp != null) {
                try {
                    resp.close();
                } catch (Exception e) {
                    // ignore
                }
            }
            if (client != null) {
                try {
                    client.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }
}
