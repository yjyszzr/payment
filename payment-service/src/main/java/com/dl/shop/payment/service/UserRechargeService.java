package com.dl.shop.payment.service;
import com.dl.member.param.RecharegeParam;
import com.dl.member.param.UpdateUserRechargeParam;
import com.dl.member.param.UserAccountParam;
import com.dl.shop.payment.core.ProjectConstant;
import com.dl.shop.payment.dao.UserRechargeMapper;
import com.dl.shop.payment.dto.RechargeUserDTO;
import com.dl.shop.payment.dto.YesOrNoDTO;
import com.dl.shop.payment.model.UserRecharge;
import lombok.extern.slf4j.Slf4j;

import com.dl.member.api.IUserAccountService;
import com.dl.member.dto.SurplusPaymentCallbackDTO;
import com.dl.member.enums.MemberEnums;
import com.alibaba.fastjson.JSON;
import com.dl.base.enums.SNBusinessCodeEnum;
import com.dl.base.exception.ServiceException;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;
import com.dl.base.util.SNGenerator;
import com.dl.base.util.SessionUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

@Service
@Transactional
@Slf4j
public class UserRechargeService extends AbstractService<UserRecharge> {
    @Resource
    private UserRechargeMapper userRechargeMapper;
    
    @Resource
    private IUserAccountService userAccountService;
 
    /**
     * 查询是否充值过：0-未充值过 1-充值过
     * @return
     */
    public BaseResult<YesOrNoDTO> countUserRecharge(){
    	Integer userId = SessionUtil.getUserId();
    	Integer count = userRechargeMapper.countUserCharge(userId);
    	YesOrNoDTO yesOrNoDTO = new YesOrNoDTO();
    	if(count == 0 ) {
    		yesOrNoDTO.setYesOrNo("0");
    	}else {
    		yesOrNoDTO.setYesOrNo("1");
    	}
    	
    	return ResultGenerator.genSuccessResult("查询是否充值成功",yesOrNoDTO);
    }
    
    
    /**
     * 创建充值单
     * @param amount
     * @return
     */
    @Transactional
    public String saveReCharege(BigDecimal amount){
    	Integer userId = SessionUtil.getUserId();
    	UserRecharge userRecharge = new UserRecharge();
    	String rechargeSn = SNGenerator.nextSN(SNBusinessCodeEnum.RECHARGE_SN.getCode());
    	userRecharge.setRechargeSn(rechargeSn);
    	userRecharge.setAmount(amount);
    	userRecharge.setUserId(userId);
    	userRecharge.setAddTime(DateUtil.getCurrentTimeLong());
    	userRecharge.setStatus(ProjectConstant.NOT_FINISH);
    	int rst = userRechargeMapper.insertUserRecharge(userRecharge);
    	if(1 != rst) {
    		log.error("保存数据库充值单失败");
    		throw new ServiceException(MemberEnums.COMMON_ERROR.getcode(), "保存数据库充值单失败");
    	}

		return rechargeSn;
    }
    
    /**
     * 根据充值单号查询充值单
     */
    public BaseResult<UserRecharge> queryUserRecharge(String rechargeSn){
    	UserRecharge userRechargeQuery = new UserRecharge();
    	userRechargeQuery.setRechargeSn(rechargeSn);
    	List<UserRecharge> userRechargeList = userRechargeMapper.queryUserChargeBySelective(userRechargeQuery);
    	if(CollectionUtils.isEmpty(userRechargeList)) {
    		return ResultGenerator.genResult(MemberEnums.DBDATA_IS_NULL.getcode(), "充值单不存在");
    	}
    	return ResultGenerator.genSuccessResult("查询充值单成功", userRechargeList.get(0));
    }
    
    
    /**
     * 更新充值单
     * @param amount
     * @return
     */
    @Transactional
    public BaseResult<String> updateReCharege(UpdateUserRechargeParam updateUserRechargeParam){
    	String inPrams = JSON.toJSONString(updateUserRechargeParam);
    	log.info(DateUtil.getCurrentDateTime()+"更新充值单参数:"+inPrams);
    	
    	UserRecharge userRecharge = new UserRecharge();
    	userRecharge.setStatus(updateUserRechargeParam.getStatus());
    	userRecharge.setRechargeSn(updateUserRechargeParam.getRechargeSn());
    	userRechargeMapper.updateUserRechargeBySelective(userRecharge);
  
		return ResultGenerator.genSuccessResult("更新数据库充值单成功");
    
    }
    
    /**
     * 构造充值赠送信息
     * @return
     */
	public RechargeUserDTO createRechargeUserDTO(){
		RechargeUserDTO rechargeUserDTO = new RechargeUserDTO();
		BaseResult<YesOrNoDTO> yesOrNotRst = this.countUserRecharge();
		Map<Integer,Integer> donationPriceMap = new HashMap<Integer,Integer>();
		YesOrNoDTO yesOrNoDTO = yesOrNotRst.getData();
		if(yesOrNoDTO.getYesOrNo().equals("0")) {
			donationPriceMap.put(10, 1);
			donationPriceMap.put(100, 10);
			donationPriceMap.put(1000, 100);
			donationPriceMap.put(6000, 800);
		}else {
			donationPriceMap.put(10, 10);
			donationPriceMap.put(20, 20);
		}
		
		rechargeUserDTO.setDonationPriceMap(donationPriceMap);
		rechargeUserDTO.setOldUserBz(Integer.valueOf(yesOrNoDTO.getYesOrNo()));
		return rechargeUserDTO;
	}

}
