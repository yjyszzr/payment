package com.dl.shop.payment.pay.jhpay.util;

import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zeming.fan@swiftpass.cn
 *
 */
public class SftSignUtil {
    private final static Logger log = LoggerFactory.getLogger(SftSignUtil.class);

    public static String formatSignData(Map<String, String> signDataMap) {
        Set<String> sortedSet = new TreeSet<String>(signDataMap.keySet());
        StringBuffer sb = new StringBuffer();
        for (String key : sortedSet) {
            if ("sign".equalsIgnoreCase(key)) {
                continue;
            }

            if (signDataMap.get(key) != null) {
                String v = String.valueOf(signDataMap.get(key));
                if (StringUtils.isNotBlank(v)) {
                    sb.append(key);
                    sb.append("=");
                    sb.append(v);
                    sb.append("&");
                }
            }

        }
        String s = sb.toString();
        if (s.length() > 0) {
            s = s.substring(0, s.length() - 1);
        }
        log.debug("To be signed data: {}", s);
        return s;
    }

    public static void verifySign(Map<String, String> signDataMap, String authMchPublicKey) {
        String sign = signDataMap.get("sign");
        if (sign == null) {
            throw new RuntimeException("WRONG_SFT_SIGN");
        }
        String toBeSignedData = formatSignData(signDataMap);
        boolean result = RSAUtil.verifySign(RSAUtil.SignatureSuite.SHA256, toBeSignedData.getBytes(CharsetUtil.UTF_8),
                Base64.getDecoder().decode(sign.getBytes(CharsetUtil.UTF_8)), authMchPublicKey);
        if (!result) {
            throw new RuntimeException("WRONG_SFT_SIGN");
        }
    }

    public static String sign(Map<String, String> signDataMap, String sftPrivateKey) {
        String toBeSignedData = formatSignData(signDataMap);
        byte[] signBuf = RSAUtil.sign(RSAUtil.SignatureSuite.SHA256, toBeSignedData.getBytes(CharsetUtil.UTF_8),
                sftPrivateKey);
        return new String(Base64.getEncoder().encode(signBuf), CharsetUtil.UTF_8);
    }
}
