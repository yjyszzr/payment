package com.dl.shop.payment.service;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.model.WithdrawLog;
import com.dl.shop.payment.dao.WithdrawLogMapper;
import com.dl.base.result.BaseResult;
import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;
import com.dl.member.param.UpdateUserWithdrawParam;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
public class WithdrawLogService extends AbstractService<WithdrawLog> {
    @Resource
    private WithdrawLogMapper withdrawLogMapper;

    //调用第三方提现
    public void withdraw() {
    	/*//调用第三方提现
    	BaseResult payBaseResult = null;

    	//处理支付失败的情况
    	if(null == payBaseResult || payBaseResult.getCode() != 0) {
    		try {
    			PayLog updatePayLog = new PayLog();
    			updatePayLog.setLogId(savePayLog.getLogId());
    			updatePayLog.setIsPaid(0);
    			updatePayLog.setPayMsg(payBaseResult.getMsg());
    			payLogService.updatePayMsg(updatePayLog);
    		} catch (Exception e) {
    			logger.error(loggerId + "paylogid="+savePayLog.getLogId()+" , paymsg="+payBaseResult.getMsg()+"保存失败记录时出错", e);
    		}
    	} else {
    		int currentTime = DateUtil.getCurrentTimeLong();
    		UpdateUserWithdrawParam updateUserWithdrawParam = new UpdateUserWithdrawParam();
    		updateUserWithdrawParam.setPaymentId(savePayLog.getLogId()+"");
    		updateUserWithdrawParam.setPayTime(currentTime);
    		updateUserWithdrawParam.setStatus("1");
    		updateUserWithdrawParam.setWithdrawalSn(orderSn);
    		BaseResult<String> updateUserWithdraw = userAccountService.updateUserWithdraw(updateUserWithdrawParam);
    		logger.info(loggerId + " paylogid="+savePayLog.getLogId()+" 提现成功回调用户提现记录更新结果 ， code="+updateUserWithdraw.getCode()+" , msg="+updateUserWithdraw.getMsg());
    	}*/
    }
}
