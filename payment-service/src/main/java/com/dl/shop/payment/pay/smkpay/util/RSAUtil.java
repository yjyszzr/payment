package com.dl.shop.payment.pay.smkpay.util;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dl.shop.payment.pay.smkpay.common.SmkPay;

import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

/**
 * RSA签名工具类
 */
@Slf4j
public class RSAUtil {
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    private static final String SIGN_CHARSET = "UTF-8";

    private static final String SIGN_TYPE_RSA = "RSA";

    private static final String SIGN_SHA256RSA_ALGORITHMS = "SHA256WithRSA";

    public static final String CERT_TYPE = "X.509";

    private static Certificate getCertformPfx(String strPfx, String strPassword) {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            FileInputStream fis = new FileInputStream(strPfx);
            // If the keystore password is empty(""), then we have to set
            // to null, otherwise it won't work!!!
            char[] nPassword = null;
            if ((strPassword == null) || strPassword.trim().equals("")) {
                nPassword = null;
            } else {
                nPassword = strPassword.toCharArray();
            }
            ks.load(fis, nPassword);
            fis.close();
            // Now we loop all the aliases, we need the alias to get keys.
            // It seems that this value is the "Friendly name" field in the
            // detals tab <-- Certificate window <-- view <-- Certificate
            // Button <-- Content tab <-- Internet Options <-- Tools menu
            // In MS IE 6.
            @SuppressWarnings("rawtypes")
            Enumeration enumas = ks.aliases();
            String keyAlias = null;
            if (enumas.hasMoreElements())// we are readin just one certificate.
            {
                keyAlias = (String) enumas.nextElement();
            }
            // Now once we know the alias, we could get the keys.
            Certificate cert = ks.getCertificate(keyAlias);
            return cert;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static PrivateKey getPvkformPfx(String strPfx, String strPassword) {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            FileInputStream fis = new FileInputStream(strPfx);
            // If the keystore password is empty(""), then we have to set
            // to null, otherwise it won't work!!!
            char[] nPassword = null;
            if ((strPassword == null) || strPassword.trim().equals("")) {
                nPassword = null;
            } else {
                nPassword = strPassword.toCharArray();
            }
            ks.load(fis, nPassword);
            fis.close();
            // Now we loop all the aliases, we need the alias to get keys.
            // It seems that this value is the "Friendly name" field in the
            // detals tab <-- Certificate window <-- view <-- Certificate
            // Button <-- Content tab <-- Internet Options <-- Tools menu
            // In MS IE 6.
            @SuppressWarnings("rawtypes")
            Enumeration enumas = ks.aliases();
            String keyAlias = null;
            if (enumas.hasMoreElements())// we are readin just one certificate.
            {
                keyAlias = (String) enumas.nextElement();
                // System.out.println("alias=[" + keyAlias + "]");
            }
            // Now once we know the alias, we could get the keys.
            PrivateKey prikey = (PrivateKey) ks.getKey(keyAlias, nPassword);
            return prikey;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * RSA2签名,用openssl生成公私钥的方式
     *
     * @param map 待签名数据和签名字段字符串
     * @return 签名
     * @throws Exception 异常上层处理
     */
    public static String rsaSign(Map<String, String> map) throws Exception {
        String content = getSignContent(map);
        String signPrivateKey = map.get("signPrivateKey");
        PrivateKey priKey = getPrivateKeyFromPKCS8(new ByteArrayInputStream(signPrivateKey.getBytes()));
        Signature signature = Signature.getInstance(SIGN_SHA256RSA_ALGORITHMS);
        signature.initSign(priKey);
        signature.update(content.getBytes(SIGN_CHARSET));
        String sign = new String(Base64.encodeBase64(signature.sign()));
        return sign;
    }


    /**
     * RSA2签名
     *
     * @param map 待签名数据和签名字段字符串
     * @return 签名
     * @throws Exception 异常上层处理
     */
    public static String rsaSignByCert(Map<String, String> map,String certPath,String certPwd) throws Exception {
        String content = getSignContent(map);

        // 获取证书
        X509Certificate x509 = (X509Certificate) getCertformPfx(certPath, certPwd);
        // 构建签名,由证书指定签名算法
        Signature sa = Signature.getInstance(SIGN_SHA256RSA_ALGORITHMS);
        // 获取私匙
        PrivateKey privateKey = getPvkformPfx(certPath, certPwd);
        sa.initSign(privateKey);
        sa.update(content.getBytes(SIGN_CHARSET));
        String sign = new String(Base64.encodeBase64(sa.sign()));
        return sign;
    }
    public static String rsaSignByCert(String certPath,String certPwd) throws Exception {
        // 获取私匙
        PrivateKey privateKey = getPvkformPfx(certPath, certPwd);
        System.out.println("privateKey============="+privateKey);
        return null;
    }
	public static void main(String[] args) {
		try {
			rsaSignByCert("C:\\Users\\DELL\\Desktop/AHSD.pfx","ahsd@cxm");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    /**
     * RSA2验签
     *
     * @param map 待验签数据、签名信息和签名字段字符串
     * @return 验签结果 true/false
     * @throws Exception 异常上层处理
     */
    public static boolean rsaCheck(Map<String, String> map) throws Exception {
        String content = getSignContent(map);
        String sign = map.get("sign");
        String publicKey = map.get("publicKey");
        PublicKey pubKey = getPublicKeyFromX509(new ByteArrayInputStream(publicKey.getBytes()));
        Signature signature = Signature.getInstance(SIGN_SHA256RSA_ALGORITHMS);
        signature.initVerify(pubKey);
        signature.update(content.getBytes(SIGN_CHARSET));
        boolean signResult = signature.verify(Base64.decodeBase64(sign.getBytes()));
        return signResult;
    }

    /**
     * 把参数按照ASCII码递增排序，如果遇到相同字符则按照第二个字符的键值ASCII码递增排序
     * 将排序后的参数与其对应值，组合成“参数=参数值”的格式，并且把这些参数用&字符连接起来
     *
     * @param sortedParams 待签名数据和签名字段字符串
     * @return 待签名字符串
     */
    private static String getSignContent(Map<String, String> sortedParams) {
        //appId,method,bizContent,生成签名所需的参数
        String[] sign_param = sortedParams.get("sign_param").split(",");
        List<String> keys = new ArrayList<String>();
        Collections.addAll(keys, sign_param);
        Collections.sort(keys);
        StringBuilder content = new StringBuilder();
        int index = 0;
        for (String key : keys) {
            String value = sortedParams.get(key);
            if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value)) {
                content.append(index == 0 ? "" : "&").append(key).append("=").append(value);
                index++;
            }
        }
        return content.toString();
    }

    private static PrivateKey getPrivateKeyFromPKCS8(InputStream ins) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(SIGN_TYPE_RSA);
        byte[] encodedKey = readText(ins).getBytes();
        encodedKey = Base64.decodeBase64(encodedKey);
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
    }

    private static PublicKey getPublicKeyFromX509(InputStream ins) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(SIGN_TYPE_RSA);
        StringWriter writer = new StringWriter();
        io(new InputStreamReader(ins), writer, -1);
        byte[] encodedKey = writer.toString().getBytes();
        encodedKey = Base64.decodeBase64(encodedKey);
        return keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
    }

    private static String readText(InputStream ins) throws IOException {
        Reader reader = new InputStreamReader(ins);
        StringWriter writer = new StringWriter();
        io(reader, writer, -1);
        return writer.toString();
    }

    private static void io(Reader in, Writer out, int bufferSize) throws IOException {
        if (bufferSize == -1) {
            bufferSize = DEFAULT_BUFFER_SIZE >> 1;
        }
        char[] buffer = new char[bufferSize];
        int amount;
        while ((amount = in.read(buffer)) >= 0) {
            out.write(buffer, 0, amount);
        }
    }

    /**
     * 验证签名
     *
     * @param data
     * @param sign
     * @param cerPath
     * @return
     * @throws Exception
     */
    public static boolean verifyByCert(String data, String sign, String cerPath) throws Exception {
        X509Certificate x509 = (X509Certificate) getCertificate(cerPath);
        Signature sa = Signature.getInstance(x509.getSigAlgName());
        sa.initVerify(x509);
        sa.update(data.getBytes("GBK"));
        return sa.verify(Base64.decodeBase64(sign.getBytes("GBK")));
    }

    /**
     *
     * @param certificatePath
     * @return Certificate 证书
     * @throws Exception
     */
    private static Certificate getCertificate(String certificatePath) throws Exception {
        CertificateFactory factory = CertificateFactory.getInstance(CERT_TYPE);
        FileInputStream in = new FileInputStream(certificatePath);
        Certificate certificate = factory.generateCertificate(in);
        in.close();
        return certificate;

    }
}
