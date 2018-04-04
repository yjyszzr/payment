package com.dl.shop.payment.web;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.member.dto.UserWithdrawDTO;
import com.dl.shop.payment.dto.UserWithdrawDetailDTO;
import com.dl.shop.payment.dto.UserWithdrawLogDTO;
import com.dl.shop.payment.model.UserWithdrawLog;
import com.dl.shop.payment.param.WithdrawDetailParam;
import com.dl.shop.payment.service.UserWithdrawLogService;

import io.swagger.annotations.ApiOperation;

/**
* Created by CodeGenerator on 2018/03/28.
*/
@RestController
@RequestMapping("/payment/withdraw")
public class UserWithdrawLogController {
    @Resource
    private UserWithdrawLogService userWithdrawLogService;

    @ApiOperation(value="提现进度详情")
    @PostMapping("/list")
    public BaseResult<UserWithdrawDetailDTO> detail(@RequestBody WithdrawDetailParam param) {
    	String accountId = param.getAccountId();
    	if(StringUtils.isBlank(accountId)) {
    		return ResultGenerator.genFailResult("提现流水号不能为空!", null);
    	}
    	UserWithdrawDTO userWithdrawDTO = null;
    	if(userWithdrawDTO == null) {
    		return ResultGenerator.genFailResult("提现流水信息为空!", null);
    	}
    	String withdawSn = userWithdrawDTO.getWithdrawalSn();
        List<UserWithdrawLogDTO> userWithdrawLogs = userWithdrawLogService.findByWithdrawSn(withdawSn);
        UserWithdrawDetailDTO dto = new UserWithdrawDetailDTO();
        dto.setAmount("1.00");
        dto.setCard("招商银行(尾号9832)");
        dto.setStatus("申请中");
        dto.setWithdrawSn(withdawSn);
        dto.setUserWithdrawLogs(userWithdrawLogs);
        return ResultGenerator.genSuccessResult(null, dto);
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
