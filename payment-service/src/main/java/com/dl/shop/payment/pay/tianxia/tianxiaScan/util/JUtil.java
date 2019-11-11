package com.dl.shop.payment.pay.tianxia.tianxiaScan.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

public class JUtil {

	private static final ObjectMapper objmap = new ObjectMapper();

	/**
	 * json 转为 map 对象
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> toMap(String json) {
		return objectFromJson(json, Map.class);
	}

	/**
	 * map 转为json 字符串 默认编码utf-8
	 * 
	 * @param map
	 * @return
	 */
	public static String toJsonString(Map<String, Object> map) {
		return mapToJson(map, "UTF8");
	}

	/**
	 * map 转为json 字符串
	 * 
	 * @param map
	 * @param charset
	 *            编码
	 * @return
	 */
	private static String mapToJson(Map<String, Object> map, String charset) {
		try {
			return new String(jsonFromObject(map, charset), charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static <T> T objectFromJson(String paramString, Class<T> paramClass) {
		JsonParser localJsonParser = null;
		T localObject1 = null;
		try {
			localJsonParser = objmap.getJsonFactory().createJsonParser(paramString);
			localObject1 = localJsonParser.readValueAs(paramClass);
		} catch (RuntimeException localRuntimeException) {

			throw localRuntimeException;
		} catch (Exception localException) {

			return null;
		} finally {
			if (localJsonParser != null)
				try {
					localJsonParser.close();
				} catch (IOException localIOException2) {
				}
		}
		return localObject1;
	}

	public static byte[] jsonFromObject(Object paramObject, String paramString) {
		ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
		JsonGenerator localJsonGenerator = null;
		try {
			localJsonGenerator = objmap.getJsonFactory().createJsonGenerator(localByteArrayOutputStream, JsonEncoding.valueOf(paramString));
			localJsonGenerator.writeObject(paramObject);
			localJsonGenerator.flush();
		} catch (RuntimeException localRuntimeException) {
			throw localRuntimeException;
		} catch (Exception localException) {

			return null;
		} finally {
			if (localJsonGenerator != null)
				try {
					localJsonGenerator.close();
				} catch (IOException localIOException2) {
				}
		}
		return localByteArrayOutputStream.toByteArray();
	}

	public static String jsonStrFromObject(Object paramObject, String paramString) {
		return new String(jsonFromObject(paramObject, paramString));

	}

	public static void main(String args[]) {
		String s = "{\"RSP_MESSAGE\":{\"REQ_BODY\":{\"telphone\":\"18701017138\",\"amt\":\"50.00\"},\"REQ_HEAD\":{\"TMP\":\"2014-05-07\",\"TIME_OUT\":60000,\"termFlowNo\":\"1223\",\"TRAN_PROCESS\":\"999555\",\"tTxnJnl\":\"1223\",\"_GAS_GLOBAL_MENU_KEY_\":\"000300\"}}}";
		System.out.println(toMap(s).toString());

	}
}
