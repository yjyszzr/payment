package com.dl.shop.payment.web;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.shop.payment.dto.UserWithdrawDetailDTO;
import com.dl.shop.payment.dto.UserWithdrawLogDTO;
import com.dl.shop.payment.enums.CashEnums;
import com.dl.shop.payment.enums.PayEnums;
import com.dl.shop.payment.model.UserWithdraw;
import com.dl.shop.payment.model.UserWithdrawLog;
import com.dl.shop.payment.param.WithdrawDetailParam;
import com.dl.shop.payment.service.UserWithdrawLogService;
import com.dl.shop.payment.service.UserWithdrawService;
import com.dl.shop.payment.utils.BankNoUtil;

import io.swagger.annotations.ApiOperation;

/**
* Created by CodeGenerator on 2018/03/28.
*/
@RestController
@RequestMapping("/payment/withdraw")
public class UserWithdrawLogController {
	private final static Logger logger = LoggerFactory.getLogger(PaymentController.class);
	
    @Resource
    private UserWithdrawLogService userWithdrawLogService;
    @Autowired
	private UserWithdrawService userWithdrawService;
    
    @ApiOperation(value="提现进度详情")
    @PostMapping("/list")
    public BaseResult<UserWithdrawDetailDTO> detail(@RequestBody WithdrawDetailParam param) {
    	String withDrawSn = param.getWithdrawSn();
    	if(StringUtils.isBlank(withDrawSn)) {
    		return ResultGenerator.genFailResult("提现流水号不能为空!", null);
    	}
    	//UserWithdrawLogDTO
    	List<UserWithdrawLogDTO> rList = userWithdrawLogService.findByWithdrawSn(withDrawSn);
    	logger.info("detail -> rList.size:" + rList.size());
    	if(rList == null || rList.size() <= 0) {
    		return ResultGenerator.genResult(PayEnums.WITHDRAW_EMPTY.getcode(), PayEnums.WITHDRAW_EMPTY.getMsg());
    	}
    	BaseResult<UserWithdraw> baseResult = userWithdrawService.queryUserWithdraw(withDrawSn);
    	UserWithdraw userWithDraw = baseResult.getData();
    	if(baseResult.getCode() != 0 && userWithDraw != null) {
        	logger.info("detail -> userWithDraw:" + userWithDraw.getRealName() + "查询提现记录失败..");
    		return ResultGenerator.genResult(PayEnums.WITHDRAW_USER_ACC_EMPTY.getcode(), PayEnums.WITHDRAW_USER_ACC_EMPTY.getMsg());
    	}else {
        	logger.info("detail -> userWithDraw:" + userWithDraw.getRealName() + "查询到提现记录..");
    	}
        UserWithdrawDetailDTO dto = new UserWithdrawDetailDTO();
        dto.setAmount(userWithDraw.getAmount().toString());
        dto.setCard(userWithDraw.getBankName()+"(尾号" + BankNoUtil.getTailFourCardNo(userWithDraw.getCardNo())+")");
        String strStatus = getStatus(rList);
        dto.setStatus(strStatus);
        dto.setWithdrawSn(withDrawSn);
        //list倒序
        rList = buildFakeList(rList);
        Collections.reverse(rList);
        dto.setUserWithdrawLogs(rList);
        return ResultGenerator.genSuccessResult(null, dto);
    }
    
    private List<UserWithdrawLogDTO> buildFakeList(List<UserWithdrawLogDTO> sList){
    	if(sList != null) {
    		if(sList.size() == 1) {
    			UserWithdrawLogDTO sEntity = new UserWithdrawLogDTO();
    			sEntity.setLogCode(CashEnums.CASH_REVIEWING.getcode());
    			sEntity.setLogName(CashEnums.CASH_REVIEWING.getMsg());
    			sEntity.setLogTime(null);
    			sList.add(sEntity);
    			
    			sEntity = new UserWithdrawLogDTO();
    			sEntity.setLogCode(CashEnums.CASH_SUCC.getcode());
    			sEntity.setLogName(CashEnums.CASH_SUCC.getMsg());
    			sEntity.setLogTime(null);
    			sList.add(sEntity);
    		}else if(sList.size() == 2) {
    			UserWithdrawLogDTO sEntity = new UserWithdrawLogDTO();
    			sEntity.setLogCode(CashEnums.CASH_SUCC.getcode());
    			sEntity.setLogName(CashEnums.CASH_SUCC.getMsg());
    			sEntity.setLogTime(null);
    			sList.add(sEntity);
    		}
    	}
    	return sList;
    }
    
    private String getStatus(List<UserWithdrawLogDTO> rList) {
    	String str = CashEnums.CASH_APPLY.getMsg();
    	for(int i = 0;i <rList.size();i++) {
    		UserWithdrawLogDTO entity = rList.get(i);
    		if(entity != null && entity.getLogCode() == CashEnums.CASH_SUCC.getcode()) {
    			str = CashEnums.CASH_SUCC.getMsg();
    			break;
    		}else if(entity != null && entity.getLogCode() == CashEnums.CASH_FAILURE.getcode()) {
    			str = CashEnums.CASH_FAILURE.getMsg();
    			break;
    		}else if(entity != null && entity.getLogCode() == CashEnums.CASH_REVIEWING.getcode()) {
    			str = CashEnums.CASH_REVIEWING.getMsg();
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
