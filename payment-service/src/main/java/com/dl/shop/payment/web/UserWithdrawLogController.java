package com.dl.shop.payment.web;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.member.dto.UserWithdrawDTO;
import com.dl.shop.payment.dto.UserWithdrawDetailDTO;
import com.dl.shop.payment.dto.UserWithdrawLogDTO;
import com.dl.shop.payment.enums.CashEnums;
import com.dl.shop.payment.enums.PayEnums;
import com.dl.shop.payment.model.UserWithdraw;
import com.dl.shop.payment.model.UserWithdrawLog;
import com.dl.shop.payment.param.WithdrawDetailParam;
import com.dl.shop.payment.service.UserWithdrawLogService;
import com.dl.shop.payment.service.UserWithdrawService;

import io.swagger.annotations.ApiOperation;

/**
* Created by CodeGenerator on 2018/03/28.
*/
@RestController
@RequestMapping("/payment/withdraw")
public class UserWithdrawLogController {
    @Resource
    private UserWithdrawLogService userWithdrawLogService;
    @Autowired
	private UserWithdrawService userWithdrawService;
    
    @ApiOperation(value="提现进度详情")
    @PostMapping("/list")
    public BaseResult<UserWithdrawDetailDTO> detail(@RequestBody WithdrawDetailParam param) {
    	String withDrawSn = param.getWithdraw_sn();
    	if(StringUtils.isBlank(withDrawSn)) {
    		return ResultGenerator.genFailResult("提现流水号不能为空!", null);
    	}
    	List<UserWithdrawLogDTO> rList = userWithdrawLogService.findByWithdrawSn(withDrawSn);
    	if(rList == null || rList.size() <= 0) {
    		return ResultGenerator.genResult(PayEnums.WITHDRAW_EMPTY.getcode(), PayEnums.WITHDRAW_EMPTY.getMsg());
    	}
    	BaseResult<UserWithdraw> baseResult = userWithdrawService.queryUserWithdraw(withDrawSn);
    	UserWithdraw userWithDraw = baseResult.getData();
    	if(baseResult.getCode() != 0 && userWithDraw != null) {
    		return ResultGenerator.genResult(PayEnums.WITHDRAW_USER_ACC_EMPTY.getcode(), PayEnums.WITHDRAW_USER_ACC_EMPTY.getMsg());
    	}
        List<UserWithdrawLogDTO> userWithdrawLogs = rList;
        UserWithdrawDetailDTO dto = new UserWithdrawDetailDTO();
        dto.setAmount(userWithDraw.getAmount().toString());
        dto.setCard(userWithDraw.getBankName()+"(尾号" + userWithDraw.getCardNo()+")");
        String strStatus = getStatus(rList);
        dto.setStatus(strStatus);
        dto.setWithdrawSn(withDrawSn);
        dto.setUserWithdrawLogs(userWithdrawLogs);
        return ResultGenerator.genSuccessResult(null, dto);
    }
    
    private String getStatus(List<UserWithdrawLogDTO> rList) {
    	String str = CashEnums.CASH_APPLY.getMsg();
    	for(int i = 0;i <rList.size();i++) {
    		UserWithdrawLogDTO entity = rList.get(i);
    		if(entity != null && entity.getLogCode() == CashEnums.CASH_SUCC.getcode()) {
    			str = CashEnums.CASH_SUCC.getMsg();
    			break;
    		}
    		if(entity != null && entity.getLogCode() == CashEnums.CASH_FAILURE.getcode()) {
    			str = CashEnums.CASH_FAILURE.getMsg();
    			break;
    		}
    		if(entity != null && entity.getLogCode() == CashEnums.CASH_REVIEWING.getcode()) {
    			str = CashEnums.CASH_REVIEWING.getMsg();
    			break;
    		}
    	}
    	return str;
    }
    
    @ApiOperation(value="添加提现进度")
    @PostMapping("/add")
    public BaseResult add(UserWithdrawLog userWithdrawLog) {
        userWithdrawLogService.save(userWithdrawLog);
        return ResultGenerator.genSuccessResult();
    }

   /* @PostMapping("/delete")
    public BaseResult delete(@RequestParam Integer id) {
        userWithdrawLogService.deleteById(id);
        return ResultGenerator.genSuccessResult();
    }

    @PostMapping("/update")
    public BaseResult update(UserWithdrawLog userWithdrawLog) {
        userWithdrawLogService.update(userWithdrawLog);
        return ResultGenerator.genSuccessResult();
    }*/

}
