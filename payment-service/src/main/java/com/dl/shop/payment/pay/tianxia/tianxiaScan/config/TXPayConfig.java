package com.dl.shop.payment.pay.tianxia.tianxiaScan.config;

import lombok.Data;

import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class TXPayConfig {
	public static String PRVKEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCEa3WZDI0l1MjSaDdCkaWWV7FRtVkXqBLfqgCCo3/lP8JG5BLyyMfWhmPXGO/RRQxSQZ0lQ58aEY/PD4sSQZV56tzwgG24kPJ0yEga9ISX7upbWISDjfWcMRFcmtXijn+Dm5sxKRcxS+5bANYbJ0bqhqlAxGAZYEHQ2SJiDpB4ZqnklXfOsTUiQk/eGV5jeyY7+goDcG59SfBwejsUk0gb7QpLRhOlwrT+974DPBZd95/Y6lj0Ga6O/9igtmF0sllDh/mhjG4UbdZ2NODNrBIkMJ3ufQmMnn/g4fIvoaWTFVQ6ZFOrzIDkJolCE63MDJOLW5YfRvzYgvvsCqPTIZqPAgMBAAECggEAN//FvJKdQklpH+norKMxVpIBL9+0LJfsW1/mVVjVZlp/S0F6befQ8vzkTfuCt4ouur56uV6OcxTILzWQh4jqsKNNCfwV6n6Jui9UpumPBT2JKFYXvBaVrpblk7b7CpNeg4aTq8eHwlYtF5kT8mm6yMVn4DbruO+HBc49hyBHolKPg2g9KS2ccbl7b5l2DWY0C29/Vpe7DfFRNUv9UEKMtyqfJvMgZSuuYxwP4hdv/wfwCPoGxy6wuWXUreATff4dCHEuCauW/FRjfiZf+ZAjmPa4xb0RSqGVuURrTBJmckx9w7pEcjYKfdRv0ObAW2e9bNsEZVyZG3GLJncdXdMYSQKBgQDs8cHjQwTeOH6MUOBgjgpi3WL60TnoounhpJzyGXPJWMvg/kCrL090WxoUQWA/P/WZlP3Gxq1VeTuX8ZiWT9hnADIuzSAkRnpzM0Y5CGlNWKM1oRXFGS/cfGsS0iqQDqkndJu91EkeVkqwDNOCac4f+LOXY0VYSpkbBNcmhgNwewKBgQCPEbxAfHZRBrH9msrkJbThilvOkJz7RnPVIlQ2pLj5rhD8EAcH0uIGcyDChRh6xZfYkkCSy1w4pPE570JuzWG8xsFjYyxRHu+quz5nxy5YMjxbNZ6ylb+Rh5mgK80hR1I6WUlyhlzetUqqoe+MMaqUc13pues/jtcrGlWCqx4D/QKBgQCmmVW6dWT22sIf5r3dJgIngYIW2QXejK3l+dhgYYUIDWdMy3is5Jb5ORUdKNqDnnURh7DoEsnNT3CXZUfkaD/ALGMchR4UY3m1hlfZwhBIUgpvqtOjwhHk9ZZTpXJH2AcXtuXKk3jV29an8lzFVZ+Y20VrLGGVst30IXMloxTIAQKBgEYm1R7tqHpMhSQsLYY3Mv1QgXnFESRoxE1i2tY8aPMMtglSto/QiMHO2+Zlqr4weydXd1BDZQHlZt8YgYOhM2aEMrlQHJ/eQnh9/biXQxM99rhmj11T7i4mxl5ye8/wj9JKi6gbWBhf3q/SXYpppBHMi+UaI87zdHAPJpS+4SXlAoGAZ5C+LqN9l1kmfKdGRAYtBV4sskg6sjP9tl7EHz194hFaPcZ2IEKesKdl1roJW78QCSHSRvUvF6iy9Hfj//KHKouGXSm/smmu0nMpCWefM79/eLBM8X90WdVVpugNI1rS8MZd4vIVxYIcUA39z9GfpIASAbuL6zLCqnIXOVtHYUM=";
	public static String DESKEY = "12456789012345678900234567890ab";
	// //平台公钥
	public static String PUBKEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlCuxOzkfeQELqEbOhF4jimFRYrCmrzEW3mfTUENghg+IPQGKdX7yqRf7nQRxPJ7J8JTa/1QUCP5j2itQ5HrXKdkn8yeRr7Jr5khRzG4i4FnNc5urrC+fHqgPLrR6/tp3VADqcUWZ3rVhd3AWZWaLQ/0DcPj2WtOjEg8141H2m56DcDTYWr3UL0EFIMrEH21o/XMeamKMB8Ma5Bhextm7qqPNG3fd2LvEg1bC1x6sIcnm3pcuRtNb1tkDgSl5LmgDNhcqF51a5XTS8Lc2iaGtpMPrcLiMupeOPdkGJf8nMOfNOFWJM4nFv0vplzcr3ZurbbpP1KyGT4lUcFb33WI+LwIDAQAB";
	public static String MD5KEY = "OYbxS2UwTTrQiuoIWCcSfDJKdIgEourZ";
	// 北京数字传奇网络科技有限公司
	public static String AGTID = "18090197";// 机构号
	public static String MERID = "1809019760";// 商户编号
	// 西安精彩数动信息科技有限公司
	// static String AGTID = "18090196";// 机构号
	// static String MERID = "1809019662";// 商户编号
	public static String REQ_URL = "http://47.75.108.4:8343/webwt/pay/gateway.do";

	// public static String CALLBACK_URL =
	// "https://api.caixiaomi.net/api/payment/payment/notify/TXCallBack";
	public static String CALLBACK_URL = "http://39.106.18.39:8765/api/payment/payment/notify/TXCallBack";

}
