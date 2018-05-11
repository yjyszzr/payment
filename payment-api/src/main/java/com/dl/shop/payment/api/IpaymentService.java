package com.dl.shop.payment.api;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.dl.base.result.BaseResult;
import com.dl.shop.payment.dto.UserWithdrawDTO;
import com.dl.shop.payment.dto.UserWithdrawDetailDTO;
import com.dl.shop.payment.param.RollbackOrderAmountParam;
import com.dl.shop.payment.param.WithDrawSnParam;

@FeignClient(value="payment-service")
public interface IpaymentService {

	@RequestMapping(path="/payment/rollbackOrderAmount", method=RequestMethod.POST)
	public BaseResult rollbackOrderAmount(@RequestBody RollbackOrderAmountParam param);
	
    /**
     * 根据提现单号查询提现单
     */
	@RequestMapping(path="/payment/querUserWithDraw", method=RequestMethod.POST)
	public BaseResult<UserWithdrawDetailDTO> querUserWithDrawDetail(WithDrawSnParam withDrawSnParam);
	
    /**
     * 根据提现单号和userId查询提现单
     */
 	@RequestMapping(path="/payment/queryUserWithdrawBySnAndUserId", method=RequestMethod.POST)
    public BaseResult<UserWithdrawDTO> queryUserWithdrawBySnAndUserId(WithDrawSnParam withDrawSn);
}
