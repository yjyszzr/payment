package com.dl.shop.payment.web;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.shop.payment.dto.UserWithdrawLogDTO;
import com.dl.shop.payment.model.UserWithdrawLog;
import com.dl.shop.payment.param.WithdrawDetailParam;
import com.dl.shop.payment.service.UserWithdrawLogService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

/**
* Created by CodeGenerator on 2018/03/28.
*/
@RestController
@RequestMapping("/payment/withdraw")
public class UserWithdrawLogController {
    @Resource
    private UserWithdrawLogService userWithdrawLogService;

    @PostMapping("/list")
    public BaseResult<List<UserWithdrawLogDTO>> detail(@RequestBody WithdrawDetailParam param) {
    	String withdawSn = param.getWithdawSn();
    	if(StringUtils.isBlank(withdawSn)) {
    		return ResultGenerator.genFailResult("提现单号不能为空!", null);
    	}
        List<UserWithdrawLogDTO> userWithdrawLog = userWithdrawLogService.findByWithdrawSn(withdawSn);
        return ResultGenerator.genSuccessResult(null, userWithdrawLog);
    }
    
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
