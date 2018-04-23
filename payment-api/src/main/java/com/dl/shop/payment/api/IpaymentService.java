package com.dl.shop.payment.api;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.dl.base.result.BaseResult;
import com.dl.shop.payment.param.RollbackOrderAmountParam;

@FeignClient(value="payment-service")
public interface IpaymentService {

	@RequestMapping(path="/payment/rollbackOrderAmount", method=RequestMethod.POST)
	public BaseResult rollbackOrderAmount(@RequestBody RollbackOrderAmountParam param);
}
