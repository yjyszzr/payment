package com.dl.shop.payment.web;
import com.dl.shop.payment.model.UserWithdraw;
import com.dl.shop.payment.param.WithDrawSnParam;
import com.dl.shop.payment.service.UserWithdrawService;
import com.dl.base.result.BaseResult;
import com.dl.base.service.AbstractService;
import com.dl.member.dto.UserWithdrawDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import javax.annotation.Resource;

@Service
@Transactional
public class UserWithdrawController extends AbstractService<UserWithdraw> {
    @Resource
    private UserWithdrawService userWithdrawService;
  
    /**
     * 根据提现单号查询提现单
     */
 	@RequestMapping(path="/payment/querUserWithDraw", method=RequestMethod.POST)
    public BaseResult<UserWithdraw> queryUserWithdraw(WithDrawSnParam withdrawSnParam){
 		return userWithdrawService.queryUserWithdraw(withdrawSnParam.getWithDrawSn());
    }
 	
    /**
     * 根据提现单号和userId查询提现单
     */
 	@RequestMapping(path="/payment/queryUserWithdrawBySnAndUserId", method=RequestMethod.POST)
    public BaseResult<UserWithdrawDTO> queryUserWithdrawBySnAndUserId(WithDrawSnParam withDrawSn){
 		return userWithdrawService.queryUserWithdrawBySnAndUserId(withDrawSn.getWithDrawSn());
 	}
  
}
