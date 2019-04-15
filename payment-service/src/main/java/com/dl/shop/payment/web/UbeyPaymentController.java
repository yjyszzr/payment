package com.dl.shop.payment.web;

import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.shop.payment.dto.PayReturnUbeyDTO;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.param.UbeyBankTypeParam;
import com.dl.shop.payment.service.PayLogService;
import com.dl.shop.payment.service.UbeyPayService;

import io.swagger.annotations.ApiOperation;

/**
* Created by CodeGenerator on 2018/03/28.
*/
@RestController
@RequestMapping("/payment/Ubey")
public class UbeyPaymentController {
	private final static Logger logger = LoggerFactory.getLogger(PaymentController.class);
	
    @Resource
    private UbeyPayService ubeyPayService;
    @Resource
	private PayLogService payLogService;
    
    @ApiOperation(value="Ubey支付请求数据")
    @PostMapping("/nUnifiedOrderUbey")
    public BaseResult<PayReturnUbeyDTO> nUnifiedOrderUbey(@RequestBody UbeyBankTypeParam param) {
    	logger.info("Ubey选择银行列表传入参数UbeyBankTypeParam={}",param);
    	if(param.getOrderId()==null||param.getOrderId().equals("")||param.getPayLogId()==null||param.getPayLogId().equals("")) {
    		return ResultGenerator.genFailResult("参数错误", null);
    	}
    	PayLog payLog = payLogService.findById(param.getPayLogId());
    	if(payLog==null) {
    		return ResultGenerator.genFailResult("请求失败", null);
    	}
        return ubeyPayService.getUBeyPayUrl(payLog,param.getOrderId());
    }
    
}
