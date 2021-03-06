package com.dl.shop.payment.api;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.dl.base.param.EmptyParam;
import com.dl.base.result.BaseResult;
import com.dl.shop.payment.dto.PayLogDTO;
import com.dl.shop.payment.dto.PayLogDetailDTO;
import com.dl.shop.payment.dto.PriceDTO;
import com.dl.shop.payment.dto.RspOrderQueryDTO;
import com.dl.shop.payment.dto.UserRechargeDTO;
import com.dl.shop.payment.dto.UserWithdrawDTO;
import com.dl.shop.payment.dto.UserWithdrawDetailDTO;
import com.dl.shop.payment.dto.ValidPayDTO;
import com.dl.shop.payment.dto.YesOrNoDTO;
import com.dl.shop.payment.param.PayLogIdParam;
import com.dl.shop.payment.param.PayLogOrderSnParam;
import com.dl.shop.payment.param.RollbackOrderAmountParam;
import com.dl.shop.payment.param.StrParam;
import com.dl.shop.payment.param.UserIdParam;
import com.dl.shop.payment.param.WithDrawSnAndUserIdParam;
import com.dl.shop.payment.param.WithDrawSnParam;

@FeignClient(value="payment-service")
public interface IpaymentService {

	@RequestMapping(path="/payment/rollbackOrderAmount", method=RequestMethod.POST)
	public BaseResult rollbackOrderAmount(@RequestBody RollbackOrderAmountParam param);
	
    /**
     * 根据提现单号查询提现单
     */
	@RequestMapping(path="/payment/withdraw/querUserWithDraw", method=RequestMethod.POST)
	public BaseResult<UserWithdrawDetailDTO> querUserWithDrawDetail(@RequestBody WithDrawSnParam withDrawSnParam);
	
	/**
     * 根据提现单号查询提现单
     */
	@RequestMapping(path="/payment/getShMoney", method=RequestMethod.POST)
	public BaseResult<RspOrderQueryDTO> getShMoney(@RequestBody StrParam strParam);
	
    /**
     * 根据提现单号和userId查询提现单
     */
 	@RequestMapping(path="/payment/withdraw/queryUserWithdrawBySnAndUserId", method=RequestMethod.POST)
    public BaseResult<UserWithdrawDTO> queryUserWithdrawBySnAndUserId(@RequestBody WithDrawSnAndUserIdParam withDrawSnAndUserIdParam);

 	/**
     * 	查询当前登录用户的充值单列表
     */
 	@RequestMapping(path="/payment/recharge/queryUserRechargeListByUserId", method=RequestMethod.POST)
    public BaseResult<UserRechargeDTO> queryUserRechargeListByUserId(@RequestBody StrParam strParam);
 	
	/**
     * 	根据payLogId查询支付信息
     */
 	@RequestMapping(path="/payment/queryPayLogByPayLogId", method=RequestMethod.POST)
    public BaseResult<PayLogDTO> queryPayLogByPayLogId(@RequestBody PayLogIdParam payLogIdParam);
 	
 	/**
     * 	根据payLogId查询支付信息
     */
 	@RequestMapping(path="/payment/queryPayLogByOrderSn", method=RequestMethod.POST)
    public BaseResult<PayLogDetailDTO> queryPayLogByOrderSn(@RequestBody PayLogOrderSnParam payLogIdParam);
 	
 	
 	/**
 	 *  查询用户是否成功充过值
 	 */
 	@RequestMapping(path="/payment/recharge/countUserRecharge", method=RequestMethod.POST)
    public BaseResult<YesOrNoDTO> countUserRecharge(@RequestBody UserIdParam userIdParam); 	
 	
 	/**
 	 * 查询用户是否充过值
 	 */
 	@RequestMapping(path="/payment/recharge/countChargeByUserId", method=RequestMethod.POST)
    public BaseResult<YesOrNoDTO> countChargeByUserId(@RequestBody UserIdParam userIdParam);

 	/**
 	 * queryPriceInRedis
 	 */
 	@RequestMapping(path="/payment/queryPriceInRedis", method=RequestMethod.POST)
    public BaseResult<PriceDTO> queryMoneyInRedis(@RequestBody PayLogIdParam payLogIdParam);

	/**
     * 	校验用户是否有过钱上的成功的交易
     */
 	@RequestMapping(path="/payment/validUserPay", method=RequestMethod.POST)
    public BaseResult<ValidPayDTO> validUserPay(@RequestBody UserIdParam userIdParam);

	/**
     * 	包含了第三方支付的超时处理
     */
	@RequestMapping(path="/payment/dealBeyondPayTimeOrderOut", method=RequestMethod.POST)
    public BaseResult<String> dealBeyondPayTimeOrderOut(@RequestBody EmptyParam emptyParam);
	
	/**
	 * 提现状态轮询
	 */
	@RequestMapping(path="/cash/timerCheckCashReq", method=RequestMethod.POST)
	public BaseResult<String> timerCheckCashReq(@RequestBody EmptyParam emptyParam);
	
	/**
	 * 第三方支付的query后的更新支付状态
	 */
	@RequestMapping(path="/payment/timerOrderQueryScheduled", method=RequestMethod.POST)
    public BaseResult<String> timerOrderQueryScheduled(@RequestBody EmptyParam emptyParam);
	/**
	 * 第三方支付的query后的更新支付状态
	 */
	@RequestMapping(path="/payment/timerRechargeQueryScheduled", method=RequestMethod.POST)
    public BaseResult<String> timerRechargeQueryScheduled(@RequestBody EmptyParam emptyParam);

}
