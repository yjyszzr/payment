package com.dl.shop.payment.pay.jhpay.util;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zeming.fan@swiftpass.cn
 *
 */
public class RSAUtil {
    public static enum CipherSuite {
        ECB_PKCS1PADDING("RSA/ECB/PKCS1Padding");

        private String suite;

        CipherSuite(String suite) {
            this.suite = suite;
        }

        public String val() {
            return suite;
        }
    }

    public static enum SignatureSuite {
        SHA1("SHA1WithRSA"),SHA256("SHA256WithRSA");

        private String suite;

        SignatureSuite(String suite) {
            this.suite = suite;
        }

        public String val() {
            return suite;
        }
    }

    private final static Logger logger = LoggerFactory.getLogger(RSAUtil.class);
    private final static KeyFactory keyFactory;

    static {
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            // 应该不会出现
            throw new RuntimeException("初始化RSA KeyFactory失败");
        }
    }

    public static byte[] encrypt(CipherSuite suite, byte[] msgBuf, String publicKeyStr) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(suite.val());
        } catch (Exception e) {
        }
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyStr));
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        } catch(Exception e) {
            logger.warn("解析公钥失败：{}", e.getMessage());
            throw new RuntimeException("INVALID_PUBKEY");
        }
        try {
            byte[] encrypted = cipher.doFinal(msgBuf);
            return encrypted;
        } catch (IllegalBlockSizeException e) {
            logger.error("加密失败，待加密消息长度过长：{}", e.getMessage());
            throw new RuntimeException("INVALID_PARAM");
        } catch (Exception e) {
            logger.error("加密失败，未知异常：{}", e.getMessage());
            throw new RuntimeException("UNKNOWN_EXC");
        }
    }

    public static byte[] decrypt(CipherSuite suite, byte[] encryptedMsgBuf, String privateKeyStr) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(suite.val());
        } catch (Exception e) {
        }

        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyStr));
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
        } catch(Exception e) {
            logger.warn("解析私钥失败：{}", e.getMessage());
            throw new RuntimeException("INVALID_PRIKEY");
        }
        try {
            byte[] decrypted = cipher.doFinal(encryptedMsgBuf);
            return decrypted;
        } catch (IllegalBlockSizeException e) {
            logger.error("解密失败，加密消息长度过长：{}", e.getMessage());
            throw new RuntimeException("INVALID_PARAM");
        } catch (BadPaddingException e) {
            logger.error("解密失败，加密消息Padding不正确：{}", e.getMessage());
            throw new RuntimeException("INVALID_PARAM");
        } catch (Exception e) {
            logger.error("解密失败，未知异常：{}", e.getMessage());
            throw new RuntimeException("UNKNOWN_EXC");
        }
    }

    public static byte[] sign(SignatureSuite suite, byte[] msgBuf, String privateKeyStr) {
        Signature signature = null;
        try {
            signature = Signature.getInstance(suite.val());
        } catch (Exception e) {
        }
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyStr));
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            signature.initSign(privateKey);
        } catch(Exception e) {
            logger.warn("解析私钥失败：{}", e.getMessage());
            throw new RuntimeException("INVALID_PRIKEY");
        }
        try {
            signature.update(msgBuf);
            return signature.sign();
        } catch (SignatureException e) {
            // 一般不会出现
            logger.error("签名失败，未知异常：{}", e.getMessage());
            throw new RuntimeException("UNKNOWN_EXC");
        }
    }

    public static boolean verifySign(SignatureSuite suite, byte[] msgBuf, byte[] sign, String publicKeyStr) {
        Signature signature = null;
        try {
            signature = Signature.getInstance(suite.val());
        } catch (Exception e) {
        }

        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyStr));
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            signature.initVerify(publicKey);
        } catch(Exception e) {
            logger.warn("解析公钥失败：{}", e.getMessage());
            throw new RuntimeException("INVALID_PUBKEY");
        }
        try {
            signature.update(msgBuf);
            return signature.verify(sign);
        } catch (SignatureException e) {
            // 一般不会出现
            logger.error("验签失败，错误的签名格式：{}", e.getMessage());
            throw new RuntimeException("INVALID_PARAM");
        }
    }
}
