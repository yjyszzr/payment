package com.dl.shop.payment.pay.smkpay.util;

import java.io.FileInputStream;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.apache.commons.codec.binary.Base64;

/**
 */
public class CertificateCoder {

	public static final String CERT_TYPE = "X.509";

	/**
	 * @param certificatePath
	 * @return Certificate 证书
	 */
	private static Certificate getCertificate(String certificatePath) {
		Certificate certificate = null;
		try {
			CertificateFactory factory = CertificateFactory.getInstance(CERT_TYPE);
			FileInputStream in = new FileInputStream(certificatePath);
			certificate = factory.generateCertificate(in);
			in.close();
			return certificate;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * 验证签名
	 * 
	 * @param data
	 * @param sign
	 * @param cerPath
	 * @return
	 */
	public static boolean verify(String data, String sign, String cerPath) {
		X509Certificate x509 = (X509Certificate) getCertificate(cerPath);
		try {
			Signature sa = Signature.getInstance(x509.getSigAlgName());
			sa.initVerify(x509);
			sa.update(data.getBytes("GBK"));
			return sa.verify(Base64.decodeBase64(sign.getBytes("GBK")));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void main(String[] args) {
		// 签名
		String signStr = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"; 
		// 验签串拼接顺序，不要变更顺序，赋值即可
		String dataStr = "reqSeq=12312313&merCode=12312321&serialNo=1231231231&orderNo=1231231231&amount=100.00&status=01&respCode=00&respDesc=success"; 
		System.out.println(CertificateCoder.verify(dataStr, signStr, "D:\\test.cer"));
	}
	
}
