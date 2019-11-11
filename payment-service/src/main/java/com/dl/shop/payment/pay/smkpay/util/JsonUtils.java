package com.dl.shop.payment.pay.smkpay.util;

import com.alibaba.fastjson.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Package com.smk.util
 * @ClassName: JsonUtils
 * @Description:
 * @version V1.1.0
 */
public class JsonUtils {
	//json格式化
	
	public static String writeJson(Object value) {
   	 String json=JSON.toJSONString(value,true);
   	 System.out.println("json格式："+json);
   	 return json;

   }
	//string to json
	public static JSONObject readJson(String ret) {
    	JSONObject result = JSONObject.fromObject(ret);
    	System.out.println("-------返回结果-------"+result);
    	return result; 

//        try {
//            Map<String, Object> readValue = mapper.readValue(ret, Map.class);
//            if (logger.isDebugEnabled()) {
//                logger.debug("json：\n%s\n", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(readValue));
//            }
//            return readValue;
//        } catch (Exception ex) {
//            logger.error("接口返回内容转JSON错误:%s", ret, ex);
//        }
//        return new HashMap<String, Object>();
    }
	/**
	 * @Title: parseMapToJson
	 * @Description:map转json，map为string，Object类型
	 * @param map
	 * @return
	 */
	public static String parseMapObjToJson(Map<String, Object> map) {
		if (map == null) {
			return null;
		}
		String json = null;
		JSONObject jsonObject = JSONObject.fromObject(map);
		json = jsonObject.toString();
		return json;
	}

	/**
	 * @Title: parseMapStrToJson
	 * @Description:map转json，map为string，string类型
	 * @param map
	 * @return
	 */
	public static String parseMapStrToJson(Map<String, String> map) {
		if (map == null) {
			return null;
		}
		String json = null;
		JSONObject jsonObject = JSONObject.fromObject(map);
		json = jsonObject.toString();
		return json;
	}

	/**
	 * @Title: parseMapObjToJsonList
	 * @Description:将Map<String,Object>转成JsonList
	 * @param map
	 * @return
	 */
	public static String parseMapObjToJsonList(Map<String, Object> map) {
		JSONArray json = JSONArray.fromObject(map);
		return json.toString();
	}

	/**
	 * @Title: parseMapStrToJsonList
	 * @Description:将Map<String,String>转成JsonList
	 * @param map
	 * @return
	 */
	public static String parseMapStrToJsonList(Map<String, String> map) {
		JSONArray json = JSONArray.fromObject(map);
		return json.toString();
	}

	/**
	 * @Title: parseListObjToJson
	 * @Description:将List<Map<String,Object>>转成Json
	 * @param list
	 * @return
	 */
	public static String parseListObjToJson(List<Map<String, Object>> list) {
		JSONArray json = JSONArray.fromObject(list);
		return json.toString();
	}

	/**
	 * @Title: parseListStrToJson
	 * @Description:将List<Map<String,String>>转成Json
	 * @param list
	 * @return
	 */
	public static String parseListStrToJson(List<Map<String, String>> list) {
		JSONArray json = JSONArray.fromObject(list);
		return json.toString();
	}

	/**
	 * @Title: parseJSONStr2Map
	 * @Description: json字符串转Map
	 * @param jsonStr
	 * @return
	 */
	public static Map<String, String> parseJSONStr2Map(String jsonStr) {
		Map<String, String> map = new HashMap<String, String>();
		JSONObject json = JSONObject.fromObject(jsonStr);
		for (Object k : json.keySet()) {
			Object v = json.get(k);
			if (v instanceof JSONArray) {
				@SuppressWarnings("unchecked")
				Iterator<JSONObject> it = ((JSONArray) v).iterator();
				while (it.hasNext()) {
					JSONObject json2 = it.next();
					map.putAll(parseJSONStr2Map(json2.toString()));
				}
			} else {
				map.put(k.toString(), (String) v);
			}
		}
		return map;
	}

	/**
	 * @Title: parseJSONStr2Map
	 * @Description: json字符串转Map
	 * @param jsonStr
	 * @return
	 */
	public static Map<String, Object> parseJSONToMap(String jsonStr) {
		Map<String, Object> map = new HashMap<String, Object>();
		JSONObject json = JSONObject.fromObject(jsonStr);
		for (Object k : json.keySet()) {
			Object v = json.get(k);
			map.put(k.toString(), v);
		}
		return map;
	}
	@SuppressWarnings("unchecked")
	public static Map<String, String> paseJsonToMapS(String json) {
		return JSON.parseObject(json, Map.class);
	}
}
