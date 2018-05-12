package com.dl.shop.payment.web;
import com.dl.shop.payment.model.UserWithdraw;
import com.dl.shop.payment.param.WithDrawSnAndUserIdParam;
import com.dl.shop.payment.param.WithDrawSnParam;
import com.dl.shop.payment.service.UserWithdrawService;
import com.dl.base.result.BaseResult;
import com.dl.base.service.AbstractService;
import com.dl.member.dto.UserWithdrawDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/payment/withdraw")
public class UserWithdrawController extends AbstractService<UserWithdraw> {
    @Resource
    private UserWithdrawService userWithdrawService;
  
    /**
     * 根据提现单号查询提现单
     */
 	@RequestMapping(path="/querUserWithDraw", method=RequestMethod.POST)
    public BaseResult<UserWithdraw> queryUserWithdraw(@RequestBody WithDrawSnParam withdrawSnParam){
 		return userWithdrawService.queryUserWithdraw(withdrawSnParam.getWithDrawSn());
    }
 	
    /**
     * 根据提现单号和userId查询提现单
     */
 	@RequestMapping(path="/queryUserWithdrawBySnAndUserId", method=RequestMethod.POST)
    public BaseResult<UserWithdrawDTO> queryUserWithdrawBySnAndUserId(@RequestBody WithDrawSnAndUserIdParam withDrawSnAndUserIdParam){
 		return userWithdrawService.queryUserWithdrawBySnAndUserId(withDrawSnAndUserIdParam.getWithDrawSn(),withDrawSnAndUserIdParam.getUserId());
 	}
  
}
