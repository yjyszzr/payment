package com.dl.shop.payment.pay.jhpay.util;

import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zeming.fan@swiftpass.cn
 *
 */
public class TripleDesUtil {
    public static enum CipherSuite {
        ECB_PKCS5PADDING("DESede/ECB/PKCS5Padding");

        private String suite;

        CipherSuite(String suite) {
            this.suite = suite;
        }

        public String val() {
            return suite;
        }
    }

    public static enum SignatureSuite {
        DESEDE("DESede");

        private String suite;

        SignatureSuite(String suite) {
            this.suite = suite;
        }

        public String val() {
            return suite;
        }
    }

    private final static Logger logger = LoggerFactory.getLogger(TripleDesUtil.class);
    private final static SecretKeyFactory keyFactory;

    static {
        try {
            keyFactory = SecretKeyFactory.getInstance("DESede");
        } catch (NoSuchAlgorithmException e) {
            // 应该不会出现
            throw new RuntimeException("初始化3DES KeyFactory失败");
        }
    }

    public static byte[] encrypt(CipherSuite suite, byte[] msgBuf, byte[] keyBuf) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(suite.val());
        } catch (Exception e) {
        }

        try {
            DESedeKeySpec keySpec = new DESedeKeySpec(keyBuf);
            Key tripleDesKey = keyFactory.generateSecret(keySpec);
            cipher.init(Cipher.ENCRYPT_MODE, tripleDesKey);
        } catch(Exception e) {
            logger.warn("解析3DesKey失败：{}", e.getMessage());
            throw new RuntimeException("INVALID_3DESKEY");
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

    public static byte[] decrypt(CipherSuite suite, byte[] encryptedMsgBuf, byte[] keyBuf) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(suite.val());
        } catch (Exception e) {
        }

        try {
            DESedeKeySpec keySpec = new DESedeKeySpec(keyBuf);
            Key tripleDesKey = keyFactory.generateSecret(keySpec);
            cipher.init(Cipher.DECRYPT_MODE, tripleDesKey);
        } catch(Exception e) {
            logger.warn("解析3DesKey失败：{}", e.getMessage());
            throw new RuntimeException("INVALID_3DESKEY");
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
}
