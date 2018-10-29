package com.dl.shop.payment.web;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.chinapay.secss.SecssConstants;
import com.chinapay.secss.SecssUtil;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.util.DateUtil;
import com.dl.shop.payment.dto.YWQuickCardPayDTO;
import com.dl.shop.payment.param.YWQucikBankPayParam;
import com.dl.shop.payment.pay.chinapay.common.Constants;
import com.dl.shop.payment.pay.chinapay.config.YWConfig;
import com.dl.shop.payment.pay.chinapay.util.HttpUtils;
import io.swagger.annotations.ApiOperation;

/**
 * 伊蚊支付
 * @date 2018.06.08
 */
@Controller
@RequestMapping("/payment/yiwen")
public class YiWenControlelr {
	
	private final static Logger logger = LoggerFactory.getLogger(YiWenControlelr.class);
	
	@Resource
	private YWConfig ywConfig;
	
    /**
     * 配置文件根路径.
     */
    private static String certBasePath = null;
	
//    /**
//     * 同步应答需要encoding的交易类型.
//     */
//    private static Set<String> encodingTransTypes = new HashSet<String>();
//    
//    static {
//        encodingTransTypes.add("0004");
//        encodingTransTypes.add("0504");
//        encodingTransTypes.add("0505");
//        encodingTransTypes.add("0606");
//        encodingTransTypes.add("0608");
//        encodingTransTypes.add("9904");
//        encodingTransTypes.add("9905");
//        
//        encodingTransTypes.add("0006");
//        encodingTransTypes.add("0505");
//        encodingTransTypes.add("0601");
//        encodingTransTypes.add("0607");
//        encodingTransTypes.add("9901");
//    }
	
	
	@ApiOperation(value="伊蚊快捷支付请求")
	@PostMapping("/quickByCard")
	@ResponseBody
	public BaseResult<YWQuickCardPayDTO> quickByCard(@RequestBody YWQucikBankPayParam param) {
		String payUrl = ywConfig.getFrontRequestUrl();
        // 请求map
        Map<String, String> sendMap = new TreeMap<String, String>();
        sendMap.put("Version", "20150922");
        sendMap.put("MerId", ywConfig.getMerId());
        sendMap.put("MerOrderNo", param.getOrderSn());
        sendMap.put("TranDate", DateUtil.getCurrentDate(DateUtil.yyyyMMdd));
        sendMap.put("TranTime", DateUtil.getCurrentTimeString(Long.valueOf(DateUtil.getCurrentTimeLong()), DateUtil.hhmmssSdf));
        sendMap.put("BusiType", "0001");
        sendMap.put("CurryNo", "CNY");
        sendMap.put("OrderAmt", param.getOrderAmount());
		this.doPack(sendMap);
		
		String res= this.doSend(payUrl, sendMap);
		YWQuickCardPayDTO dto = new YWQuickCardPayDTO();
		dto.setShowHtmlData(res);
		dto.setFontNoticeUrl("");
		return ResultGenerator.genSuccessResult("success",dto);
	}
	
	@ApiOperation(value="伊蚊快捷支付结果查询接口")
	@PostMapping("/quickPayResultQuery")
	@ResponseBody
	public BaseResult<YWQuickCardPayDTO> quickPayResultQuery(@RequestBody YWQucikBankPayParam param) {
		String queryResultUrl = ywConfig.getQueryRequestUrl();
		
        // 请求map
        Map<String, String> sendMap = new TreeMap<String, String>();
        sendMap.put("Version", "20150922");
        sendMap.put("MerId", ywConfig.getMerId());
        sendMap.put("MerOrderNo", param.getOrderSn());
        sendMap.put("TranDate", DateUtil.getCurrentDate(DateUtil.yyyyMMdd));
        sendMap.put("TranType", "0502");
        sendMap.put("BusiType", "0001");
		
		this.doPack(sendMap);
		String res= this.doSend(queryResultUrl, sendMap);
		YWQuickCardPayDTO dto = new YWQuickCardPayDTO();
		return ResultGenerator.genSuccessResult("success",dto);
	}

    /**
     * 组包.
     * 
     * @param request .
     * @param response .
     */
    protected Map<String, String> doPack(Map<String, String> sendMap) {  	
        try {
            // 机构或商户接入
            String instuId = sendMap.get(Constants.INSTU_ID);
            String merId = sendMap.get(Constants.MER_ID);
            String ownerId = null;
            if (StringUtils.isNotEmpty(instuId)) {
                ownerId = instuId;
            } else {
                ownerId = merId;
            }
            // 获得签名加密类
            SecssUtil secssUtil = getSecssUtil(ownerId);

            // 签名
            secssUtil.sign(sendMap);
            if (SecssConstants.SUCCESS.equals(secssUtil.getErrCode())) {
                sendMap.put(Constants.SIGNATURE, secssUtil.getSign());
            } else {
                return sendMap;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
		return sendMap;
    }
	
	
	
    /**
     * 
     * .
     * @param request .
     * @param response .
     * @throws ServletException .
     * @throws IOException .
     */
    protected String doSend(String requestUrl,Map<String, String> sendMap) {
        Map<String, String> resultMap = new TreeMap<String, String>();
        String resp = HttpUtils.send(requestUrl, sendMap);
        return resp;
    }
    
    /**
     * 
     * .
     * @param request .
     * @param response .
     * @throws ServletException .
     * @throws IOException .
     */
    protected void doUnPack(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        // 通知类型
        String notifyType = request.getParameter(Constants.SPEC_NOTIFY_TYPE);
        if(StringUtils.isEmpty(notifyType)) {
            notifyType = Constants.NOTIFY_TYPE_BACK;
        }
        
        Map<String, String> resultMap = new TreeMap<String, String>();
        Enumeration<String> paraNames = request.getParameterNames();
        while (paraNames.hasMoreElements()) {
            String key = paraNames.nextElement();

            // 跳过自定义字段
            if (key.startsWith(Constants.SPEC_PRIFEX)) {
                continue;
            }
            // 跳过空字段
            String value = request.getParameter(key);
            if (StringUtils.isEmpty(value)) {
                continue;
            }
            
            // 后台通知需要解码,正式使用建议前后台接收通知地址分开
            if(Constants.NOTIFY_TYPE_BACK.equals(notifyType)) {
                value = URLDecoder.decode(value, Constants.ENCODING);
            }
            resultMap.put(key, value);
        }
        
        // 按机构或商户验签
        String instuId = resultMap.get(Constants.INSTU_ID);
        String merId = resultMap.get(Constants.MER_ID);
        String ownerId = null;
        if (StringUtils.isNotEmpty(instuId)) {
            ownerId = instuId;
        } else {
            ownerId = merId;
        }
        SecssUtil secssUtil = this.getSecssUtil(ownerId);
        secssUtil.verify(resultMap);
        if (SecssConstants.SUCCESS.equals(secssUtil.getErrCode())) {
            for (Map.Entry<String, String> entry : resultMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (StringUtils.isEmpty(value)) {
                    continue;
                }
                // 解析卡保留域
                if (Constants.CARD_TRAN_DATA.equals(key)) {
                    secssUtil.decryptData(value);
                    if (SecssConstants.SUCCESS.equals(secssUtil.getErrCode())) {
                        value = secssUtil.getDecValue();
                        // value = new String(Base64.decodeBase64(value),
                        // Constants.ENCODING);
                    } else {
                        request.setAttribute(Constants.RESP_CODE,
                                secssUtil.getErrCode());
                        request.setAttribute(Constants.RESP_MSG,
                                secssUtil.getErrMsg());
                        request.getRequestDispatcher("/page/showError.jsp")
                                .forward(request, response);
                        return;
                    }
                    resultMap.put(key, value);
                }
                if (Constants.RISK_DATA.equals(key)) {
                    secssUtil.decryptData(value);
                    if (SecssConstants.SUCCESS.equals(secssUtil.getErrCode())) {
                        value = secssUtil.getDecValue();
                        // value = new String(Base64.decodeBase64(value),
                        // Constants.ENCODING);
                    } else {
                        request.setAttribute(Constants.RESP_CODE,
                                secssUtil.getErrCode());
                        request.setAttribute(Constants.RESP_MSG,
                                secssUtil.getErrMsg());
                        request.getRequestDispatcher("/page/showError.jsp")
                                .forward(request, response);
                        return;
                    }
                    resultMap.put(key, value);
                }
            }
            logger.info(String.format("resultMap=%s", resultMap));
            request.setAttribute(Constants.RESULT_MAP, resultMap);
            request.getRequestDispatcher("/page/showResult.jsp").forward(
                    request, response);
        } else {
            request.setAttribute(Constants.RESP_CODE, secssUtil.getErrCode());
            request.setAttribute(Constants.RESP_MSG, secssUtil.getErrMsg());
            request.getRequestDispatcher("/page/showError.jsp").forward(
                    request, response);
        }

    }
    
    
    /**
     * 加载安全秘钥 .
     * 
     * @param ownerId
     *            所有者id
     * @return SecssUtil .
     */
    protected SecssUtil getSecssUtil(String ownerId) {
        String path = String.format("%s/%s.properties", certBasePath, ownerId);
        SecssUtil secssUtil = new SecssUtil();
        secssUtil.init(path);
        return secssUtil;
    }
    
    
}
