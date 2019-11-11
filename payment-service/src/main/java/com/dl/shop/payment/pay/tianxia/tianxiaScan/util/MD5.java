package com.dl.shop.payment.pay.tianxia.tianxiaScan.util;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author  
 */
public class MD5 {
	public static void main(String[] args) {
		String str = "accountName=e5bca0e6b5b7e99c9e&accountNo=37650188000136170&agtId=12000046&bankBranch=e4b8ade59bbde58589e5a4a7e993b6e8a18ce882a1e4bbbde69c89e99990e585ace58fb8e58c97e4baace69c9de998b3e694afe8a18c&bankCity=110000&bankName=e4b8ade59bbde58589e5a4a7e993b6e8a18ce882a1e4bbbde69c89e99990e585ace58fb8e58c97e4baace69c9de998b3e694afe8a18c&bankProvince=110000&bankType=00&businessLicense=91110105062775530G&certNo=130403197802182726&certType=1&cnaps=303100000071&contactMobile=13142027695&contactName=e5bca0e6b5b7e99c9e&contactPhone=13142027695&legalPersonId=130403197802182726&legalPersonName=e5bca0e6b5b7e99c9e&merchantAddress=e58c97e4baace69c9de998b3e58cbae991abe58586e4bdb3e59bad3135e58fb7e6a5bc3130e5b18231313136&merchantCity=110000&merchantName=e58c97e4baace7bf94e79dbfe4bfa1e8af9ae7a791e68a80e69c89e99990e585ace58fb8&merchantOperateType=01&merchantProvince=110000&merId=1200000046&mobile=000022&nonceStr=9AJKj7a14fZyoLl4&servicePhone=010-76849571&shortName=e7bf94e79dbfe4bfa1e8af9a&tranCode=0101&key=MitXbDVDovomeBQTNn4NaP2VmJURYMTk"+"";
		String str1 = "accountName=e5bca0e6b5b7e99c9e&accountNo=37650188000136170&agtId=12000046&bankBranch=e4b8ade59bbde58589e5a4a7e993b6e8a18ce882a1e4bbbde69c89e99990e585ace58fb8e58c97e4baace69c9de998b3e694afe8a18c&bankCity=110000&bankName=e4b8ade59bbde58589e5a4a7e993b6e8a18ce882a1e4bbbde69c89e99990e585ace58fb8e58c97e4baace69c9de998b3e694afe8a18c&bankProvince=110000&bankType=00&businessLicense=91110105062775530G&certNo=130403197802182726&certType=1&cnaps=303100000071&contactMobile=13142027695&contactName=e5bca0e6b5b7e99c9e&contactPhone=13142027695&legalPersonId=130403197802182726&legalPersonName=e5bca0e6b5b7e99c9e&merchantAddress=e58c97e4baace69c9de998b3e58cbae991abe58586e4bdb3e59bad3135e58fb7e6a5bc3130e5b18231313136&merchantCity=110000&merchantName=e58c97e4baace7bf94e79dbfe4bfa1e8af9ae7a791e68a80e69c89e99990e585ace58fb8&merchantOperateType=01&merchantProvince=110000&merId=1200000046&mobile=000022&nonceStr=2lTg7Ce9Edy9FErm&servicePhone=010-76849571&shortName=e7bf94e79dbfe4bfa1e8af9a&tranCode=0101&key=MitXbDVDovomeBQTNn4NaP2VmJURYMTk";
		System.out.println(encryption(str));
		System.out.println(encryption(str1));
	}
/**
     *
     * @param plainText
     *            明文
     * @return 32位密文
     */
    public static String encryption(String plainText) {
        String re_md5 = new String();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();

            int i;

            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }

            re_md5 = buf.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return re_md5;
    }

}
