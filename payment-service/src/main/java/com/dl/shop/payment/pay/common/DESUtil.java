package com.dl.shop.payment.pay.common;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.crypto.codec.Base64;

public class DESUtil {
	private static final String TAG = "DESUtil";
	public static final String DES_KEY = "adiop&^p";
	
    /** 
     * 生成密钥 
     */  
    public static byte[] initKey() {  
        try {  
            //KeyGenerator 密钥生成器  
            KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");  
            //初始化密钥生成器  
            keyGenerator.init(56);  
            //生成密钥  
            SecretKey secretKey = keyGenerator.generateKey();  
            return secretKey.getEncoded();  
        } catch (Exception e) {  
        	String tips = e.getMessage();
        	System.out.println(tips);
        }  
        return null;  
    }  
  
    /** 
     * DES加密 
     * 
     * @param data 需要加密的数据 
     * @param key  加密使用的密钥 
     * @return 加密后获取的字节数组 
     */  
    public static byte[] encrypt(byte[] data, byte[] key) {  
        //恢复密钥  
        SecretKey secretKey = new SecretKeySpec(key, "DES");  
        try {  
            //Cipher完成加密或解密工作  
            Cipher cipher = Cipher.getInstance("DES");  
            //根据密钥对Cipher进行初始化 ENCRYPT_MODE, DECRYPT_MODE  
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);  
            //加密  
            return cipher.doFinal(data);  
        } catch (Exception e) {  
        	String tips = e.getMessage();
        	System.out.println(tips);
        }  
        return null;  
    }  
  
    /** 
     * DES解密 
     */  
    /** 
     * @param data 密文对应的字节数组 
     * @param key  算法名字 
     * @return 解密后的字节数组 
     */  
    public static byte[] decrypt(byte[] data, byte[] key) {  
        SecretKey secretKey = new SecretKeySpec(key, "DES");  
        try {  
            Cipher cipher = Cipher.getInstance("DES");  
            cipher.init(Cipher.DECRYPT_MODE, secretKey);  
            return cipher.doFinal(data);  
        } catch (Exception e) {  
        	String tips = e.getMessage();
        	System.out.println(tips);
        }  
        return null;  
    }
    
    //27c5aa4b378dc9a1b883b688ce92799d
    public static void main(String[] args) {
//    	long time = System.currentTimeMillis();
//    	String str = MD5Utils.MD5(time+"");
//    	System.out.println(str);
    	byte[] key = "adiop&^p".getBytes();
    	String src = "HelloWorld";
    	byte[] data = encrypt(src.getBytes(), key);
    	System.out.println("data:" + new String(data) + "key.length:" + key.length);
    	byte[] r = decrypt(data,key);
    	System.out.println("result:" + new String(r));
    }
    
    private static void showArray(byte[] array) {
    	for(int i = 0;i < array.length;i++) {
    		System.out.print(array[0]);
    	}
    }
}
