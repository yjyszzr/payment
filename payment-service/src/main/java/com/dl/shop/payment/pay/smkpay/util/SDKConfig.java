package com.dl.shop.payment.pay.smkpay.util;

import java.io.*;
import java.util.Properties;

/**
 * Function: ADD FUNCTION.
 * date: 2019/4/26 9:16
 *
 * @author yejqiiu
 * @version 1.0.0
 * @Copyright (c) 2019, 杭州市民卡有限公司  All Rights Reserved.
 **/
public class SDKConfig {

	private String requestUrl;
	private String merCode;
	private String appId;
	private String vertifyPublicKey;
	private String signPrivateKey;
	private String certPath;
	private String certPwd;
	private String asynVertifySignCertPath;

	public static final String SDK_REQUESTURL = "requestUrl";
	public static final String SDK_MERCODE = "merCode";
	public static final String SDK_APPID = "appId";
	public static final String SDK_VERTIFYPUBLICKEY = "vertifyPublicKey";
	public static final String SDK_SIGNPRIVATEKEY = "signPrivateKey";
	public static final String SDK_CERTPATH = "certPath";
	public static final String SDK_CERTPWD = "certPwd";
	public static final String SDK_ASYNVERTIFYSIGNCERTPATH = "asynVertifySignCertPath";

	public String getRequestUrl() {
		return requestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	public String getMerCode() {
		return merCode;
	}

	public void setMerCode(String merCode) {
		this.merCode = merCode;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getVertifyPublicKey() {
		return vertifyPublicKey;
	}

	public void setVertifyPublicKey(String vertifyPublicKey) {
		this.vertifyPublicKey = vertifyPublicKey;
	}

	public String getSignPrivateKey() {
		return signPrivateKey;
	}

	public void setSignPrivateKey(String signPrivateKey) {
		this.signPrivateKey = signPrivateKey;
	}

	public String getCertPath() {
		return certPath;
	}

	public void setCertPath(String certPath) {
		this.certPath = certPath;
	}

	public String getCertPwd() {
		return certPwd;
	}

	public void setCertPwd(String certPwd) {
		this.certPwd = certPwd;
	}

	public String getAsynVertifySignCertPath() {
		return asynVertifySignCertPath;
	}

	public void setAsynVertifySignCertPath(String asynVertifySignCertPath) {
		this.asynVertifySignCertPath = asynVertifySignCertPath;
	}

	/** 操作对象. */
	private static SDKConfig config = new SDKConfig();
	/** 属性文件对象. */
	private Properties properties;

	private SDKConfig() {
		super();
	}

	/**
	 * 获取config对象.
	 * @return
	 */
	public static SDKConfig getConfig() {
		return config;
	}
	/**
	 * 从classpath路径下加载配置参数
	 */
	public void loadPropertiesFromSrc(String fileName) {
		InputStream in = null;
		try {
			//logger.info("从classpath: " +SDKConfig.class.getClassLoader().getResource("").getPath()+" 获取属性文件"+FILE_NAME);
			in = SDKConfig.class.getClassLoader().getResourceAsStream(fileName);
			if (null != in) {
				properties = new Properties();
				try {
					properties.load(in);
				} catch (IOException e) {
					throw e;
				}
			} else {
				System.out.println("属性文件未能在classpath指定的目录下找到!");
				return;
			}
			loadProperties(properties);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 从properties文件加载
	 *
	 * @param filePath
	 *            不包含文件名的目录.
	 */
	public void loadPropertiesFromPath(String filePath) {
			File file = new File(filePath);
			InputStream in = null;
			if (file.exists()) {
				try {
					in = new FileInputStream(file);
					properties = new Properties();
					properties.load(in);
					loadProperties(properties);
				} catch (FileNotFoundException e) {
				} catch (IOException e) {
				} finally {
					if (null != in) {
						try {
							in.close();
						} catch (IOException e) {
						}
					}
				}
			} else {
				// 由于此时可能还没有完成LOG的加载，因此采用标准输出来打印日志信息

			}

	}

	/**
	 * 根据传入的 {@link #(Properties)}对象设置配置参数
	 *
	 * @param pro
	 */
	public void loadProperties(Properties pro) {
		String value = null;

		value = pro.getProperty(SDK_REQUESTURL);
		this.requestUrl = value.trim();

		value = pro.getProperty(SDK_MERCODE);
		this.merCode = value.trim();

		value = pro.getProperty(SDK_APPID);
		this.appId = value.trim();

		value = pro.getProperty(SDK_VERTIFYPUBLICKEY);
		this.vertifyPublicKey = value.trim();

		value = pro.getProperty(SDK_SIGNPRIVATEKEY);
		this.signPrivateKey = value.trim();

		value = pro.getProperty(SDK_CERTPATH);
		this.certPath = value.trim();

		value = pro.getProperty(SDK_CERTPWD);
		this.certPwd = value.trim();

		value = pro.getProperty(SDK_ASYNVERTIFYSIGNCERTPATH);
		this.asynVertifySignCertPath = value.trim();
	}
}
