package com.dl.shop.payment.pay.apay.util;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class SecurityUtil {

	public static class RsaKeyPair {
		private String publicKey = "";
		private String privateKey = "";

		public RsaKeyPair(String publicKey, String privateKey) {
			super();
			this.publicKey = publicKey;
			this.privateKey = privateKey;
		}

		public String getPublicKey() {
			return publicKey;
		}

		public String getPrivateKey() {
			return privateKey;
		}
	}

	private static final String ALGORITHM = "RSA";
	private static final String ALGORITHMS_SHA1WithRSA = "SHA1WithRSA";
	private static final String ALGORITHMS_SHA256WithRSA = "SHA256WithRSA";
	private static final String DEFAULT_CHARSET = "UTF-8";

	private static String getAlgorithms(boolean isRsa2) {
		return isRsa2 ? ALGORITHMS_SHA256WithRSA : ALGORITHMS_SHA1WithRSA;
	}

	/**
	 * 生成秘钥对
	 * 
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static RsaKeyPair generaterKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator keygen = KeyPairGenerator.getInstance(ALGORITHM);
		SecureRandom random = new SecureRandom();
		// SecureRandom random = new SecureRandom(seedStr.getBytes()); //
		// 随机因子一样，生成出来的秘钥会一样
		// 512位已被破解，用1024位,最好用2048位
		keygen.initialize(2048, random);
		// 生成密钥对
		KeyPair keyPair = keygen.generateKeyPair();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		String privateKeyStr = Base64.getEncoder().encodeToString(privateKey.getEncoded());
		String publicKeyStr = Base64.getEncoder().encodeToString(publicKey.getEncoded());
		return new RsaKeyPair(publicKeyStr, privateKeyStr);
	}

	/**
	 * 获取公钥
	 * 
	 * @param publicKey
	 * @return
	 * @throws Exception
	 */
	public static RSAPublicKey getPublicKey(String publicKey) throws Exception {
		byte[] keyBytes = Base64.getDecoder().decode(publicKey);
		X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
		return (RSAPublicKey) keyFactory.generatePublic(spec);
	}

	/**
	 * 获取私钥
	 * 
	 * @param privateKey
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws Exception
	 */
	public static RSAPrivateKey getPrivateKey(String privateKey) throws Exception {
		byte[] keyBytes = Base64.getDecoder().decode(privateKey);
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
		return (RSAPrivateKey) keyFactory.generatePrivate(spec);
	}

	/**
	 * 私钥签名
	 * 
	 * @throws InvalidKeySpecException
	 * @throws Exception
	 */
	public static String sign(String content, String privateKey, boolean isRsa2) throws Exception {
		// PrivateKey priKey = getPrivateKey(privateKey);
		//
		// Signature signature = Signature.getInstance("SHA1withRSA");
		// signature.initSign(priKey);
		// signature.update(content.getBytes(DEFAULT_CHARSET));
		// // signature.sign();
		// // return byte2hex(signature.sign());
		// byte[] signed = signature.sign();
		// return Base64.getEncoder().encodeToString(signed);
		PrivateKey priKey = getPrivateKey(privateKey);
		java.security.Signature signature = java.security.Signature.getInstance(getAlgorithms(isRsa2));
		signature.initSign(priKey);
		signature.update(content.getBytes(DEFAULT_CHARSET));
		byte[] signed = signature.sign();
		return Base64.getEncoder().encodeToString(signed);
	}

	private static String hexStr = "0123456789ABCDEF";

	/**
	 * 
	 * @param bytes
	 * @return 将二进制数组转换为十六进制字符串 2-16
	 */
	public static String bin2HexStr(byte[] bytes) {

		String result = "";
		String hex = "";
		for (int i = 0; i < bytes.length; i++) {
			// 字节高4位
			hex = String.valueOf(hexStr.charAt((bytes[i] & 0xF0) >> 4));
			// 字节低4位
			hex += String.valueOf(hexStr.charAt(bytes[i] & 0x0F));
			result += hex; // +" "
		}
		return result;
	}

	/**
	 * 公钥验签
	 */
	public static boolean verify(String content, String sign, String publicKey, boolean isRsa2) throws Exception {
		PublicKey pubKey = getPublicKey(publicKey);
		java.security.Signature signature = java.security.Signature.getInstance(getAlgorithms(isRsa2));
		signature.initVerify(pubKey);
		signature.update(content.getBytes(DEFAULT_CHARSET));
		return signature.verify(Base64.getDecoder().decode(sign));
	}

	/**
	 * 加密
	 * 
	 * @param input
	 * @param pubOrPrikey
	 * @return
	 */
	public static String encrypt(String content, Key pubOrPrikey) throws Exception {
		Cipher cipher = null;
		cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, pubOrPrikey);
		byte[] result = cipher.doFinal(content.getBytes(DEFAULT_CHARSET));
		return Base64.getEncoder().encodeToString(result);
	}

	/**
	 * 公钥加密
	 * 
	 * @param input
	 * @param pubOrPrikey
	 * @return
	 */
	public static String encrypt(String content, String publicKeyStr) throws Exception {
		Cipher cipher = null;
		cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(publicKeyStr));
		byte[] result = cipher.doFinal(content.getBytes(DEFAULT_CHARSET));
		return Base64.getEncoder().encodeToString(result);
	}

	/**
	 * 解密
	 * 
	 * @param input
	 * @param pubOrPrikey
	 * @return
	 */
	public static String decrypt(String content, Key pubOrPrikey) throws Exception {
		Cipher cipher = null;
		cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, pubOrPrikey);
		byte[] result = cipher.doFinal(Base64.getDecoder().decode(content));
		return new String(result);
	}

	/**
	 * 私钥解密
	 * 
	 * @param input
	 * @param pubOrPrikey
	 * @return
	 */
	public static String decrypt(String content, String privatekeyStr) throws Exception {
		Cipher cipher = null;
		cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(privatekeyStr));
		byte[] result = cipher.doFinal(Base64.getDecoder().decode(content));
		return new String(result);
	}

}