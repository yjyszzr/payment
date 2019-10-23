package com.dl.shop.payment.service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.dl.base.enums.SNBusinessCodeEnum;
import com.dl.base.exception.ServiceException;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.service.AbstractService;
import com.dl.base.util.DateUtil;
import com.dl.base.util.SNGenerator;
import com.dl.base.util.SessionUtil;
import com.dl.member.api.IUserAccountService;
import com.dl.member.enums.MemberEnums;
import com.dl.member.param.UpdateUserRechargeParam;
import com.dl.shop.payment.core.ProjectConstant;
import com.dl.shop.payment.dao.UserRechargeMapper;
import com.dl.shop.payment.dto.DonationPriceDTO;
import com.dl.shop.payment.dto.RechargeUserDTO;
import com.dl.shop.payment.dto.YesOrNoDTO;
import com.dl.shop.payment.model.UserRecharge;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class UserRechargeService extends AbstractService<UserRecharge> {
    @Resource
    private UserRechargeMapper userRechargeMapper;
    
    @Resource
    private IUserAccountService userAccountService;
 
    /**
     * 查询是否成功充值过：0-未充值过 1-充值过
     * @return
     */
    public BaseResult<YesOrNoDTO> countUserRecharge(Integer userId){
    	Integer sessionUserId = SessionUtil.getUserId();
    	if(null != sessionUserId) {
    		userId = sessionUserId;
    	}
    	
    	Integer count = userRechargeMapper.countChargeByUserId(userId);
    	YesOrNoDTO yesOrNoDTO = new YesOrNoDTO();
    	if(count == 0 ) {
    		yesOrNoDTO.setYesOrNo(ProjectConstant.noRecharged);
    	}else {
    		yesOrNoDTO.setYesOrNo(ProjectConstant.hasRecharged);
    	}
    	
    	return ResultGenerator.genSuccessResult("查询是否充值成功",yesOrNoDTO);
    }
    
    /**
     * 给充值完后的调用查询是否充值过：0-未充值过 1-充值过
     * @return
     */
    public BaseResult<YesOrNoDTO> countChargeByUserId(Integer userId){
    	Integer count = userRechargeMapper.countChargeByUserId(userId);
    	YesOrNoDTO yesOrNoDTO = new YesOrNoDTO();
    	if(count == 1 ) {
    		yesOrNoDTO.setYesOrNo(ProjectConstant.noRecharged);
    	}else if(count > 1){
    		yesOrNoDTO.setYesOrNo(ProjectConstant.hasRecharged);
    	}
    	
    	return ResultGenerator.genSuccessResult("查询是否充值成功",yesOrNoDTO);
    }
    
    
    /**
     * 创建充值单
     * @param amount
     * @return
     */
    @Transactional
    public String saveReCharege(BigDecimal amount,String payCode,String payName){
    	Integer userId = SessionUtil.getUserId();
    	UserRecharge userRecharge = new UserRecharge();
    	String rechargeSn = SNGenerator.nextSN(SNBusinessCodeEnum.RECHARGE_SN.getCode());
    	userRecharge.setRechargeSn(rechargeSn);
    	userRecharge.setAmount(amount);
    	userRecharge.setUserId(userId);
    	userRecharge.setAddTime(DateUtil.getCurrentTimeLong());
    	userRecharge.setStatus(ProjectConstant.NOT_FINISH);
    	userRecharge.setPaymentCode(payCode);
    	userRecharge.setPaymentName(payName);
    	int rst = userRechargeMapper.insertUserRecharge(userRecharge);
    	if(1 != rst) {
    		log.error("保存数据库充值单失败");
    		throw new ServiceException(MemberEnums.COMMON_ERROR.getcode(), "保存数据库充值单失败");
    	}

		return rechargeSn;
    }
    
    /**
     * 创建充值单
     * @param amount
     * @return
     */
    @Transactional
    public String saveReCharege(BigDecimal amount,String payCode,String payName,String rechargeSn,Integer userId){
    	UserRecharge userRecharge = new UserRecharge();
    	userRecharge.setRechargeSn(rechargeSn);
    	userRecharge.setAmount(amount);
    	userRecharge.setUserId(userId);
    	userRecharge.setAddTime(DateUtil.getCurrentTimeLong());
    	userRecharge.setStatus(ProjectConstant.NOT_FINISH);
    	userRecharge.setPaymentCode(payCode);
    	userRecharge.setPaymentName(payName);
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
    	userRecharge.setPaymentId(updateUserRechargeParam.getPaymentId());
    	userRecharge.setPayTime(updateUserRechargeParam.getPayTime());
    	userRechargeMapper.updateUserRechargeBySelective(userRecharge);
  
		return ResultGenerator.genSuccessResult("更新数据库充值单成功");
    
    }
    
    /**
     * 构造充值赠送信息
     * @return
     */
	public RechargeUserDTO createRechargeUserDTO(Integer userId){
		RechargeUserDTO rechargeUserDTO = new RechargeUserDTO();
		BaseResult<YesOrNoDTO> yesOrNotRst = this.countUserRecharge(userId);
		YesOrNoDTO yesOrNoDTO = yesOrNotRst.getData();
		List<DonationPriceDTO> donationPriceList = new ArrayList<DonationPriceDTO>();
		if(ProjectConstant.hasRecharged.equals(yesOrNoDTO.getYesOrNo())) {
			DonationPriceDTO donationPriceDTO = new DonationPriceDTO();
			donationPriceDTO.setMinRechargeAmount(10);
			donationPriceDTO.setDonationAmount(1);
			
			DonationPriceDTO donationPriceDTO1 = new DonationPriceDTO();
			donationPriceDTO1.setMinRechargeAmount(100);
			donationPriceDTO1.setDonationAmount(10);
			
			DonationPriceDTO donationPriceDTO2 = new DonationPriceDTO();
			donationPriceDTO2.setMinRechargeAmount(1000);
			donationPriceDTO2.setDonationAmount(100);
			
			DonationPriceDTO donationPriceDTO3 = new DonationPriceDTO();
			donationPriceDTO3.setMinRechargeAmount(6000);
			donationPriceDTO3.setDonationAmount(800);			
			
			donationPriceList.add(donationPriceDTO);
			donationPriceList.add(donationPriceDTO1);
			donationPriceList.add(donationPriceDTO2);
			donationPriceList.add(donationPriceDTO3);
		}else {
			DonationPriceDTO donationPriceDTO = new DonationPriceDTO();
			donationPriceDTO.setMinRechargeAmount(10);
			donationPriceDTO.setDonationAmount(10);

			DonationPriceDTO donationPriceDTO1 = new DonationPriceDTO();
			donationPriceDTO1.setMinRechargeAmount(20);
			donationPriceDTO1.setDonationAmount(20);
			
			DonationPriceDTO donationPriceDTO2 = new DonationPriceDTO();
			donationPriceDTO2.setMinRechargeAmount(1000);
			donationPriceDTO2.setDonationAmount(100);
			
			DonationPriceDTO donationPriceDTO3 = new DonationPriceDTO();
			donationPriceDTO3.setMinRechargeAmount(6000);
			donationPriceDTO3.setDonationAmount(800);	
			
			donationPriceList.add(donationPriceDTO);
			donationPriceList.add(donationPriceDTO1);
			donationPriceList.add(donationPriceDTO2);
			donationPriceList.add(donationPriceDTO3);
		}
		
		rechargeUserDTO.setDonationPriceList(donationPriceList);
		rechargeUserDTO.setOldUserBz(Integer.valueOf(yesOrNoDTO.getYesOrNo()));
		return rechargeUserDTO;
	}

}
