package com.dl.shop.payment.service;
import com.dl.shop.payment.core.ProjectConstant;
import com.dl.shop.payment.dao.UserWithdrawMapper;
import com.dl.shop.payment.model.UserWithdraw;
import com.dl.shop.payment.param.UpdateUserWithdrawParam;
import com.dl.shop.payment.param.UserWithdrawParam;

import lombok.extern.slf4j.Slf4j;

import com.dl.member.api.IUserAccountService;
import com.dl.member.api.IUserBankService;
import com.dl.member.dto.UserBankDTO;
import com.dl.member.dto.UserWithdrawDTO;
import com.dl.member.dto.WithdrawalSnDTO;
import com.dl.member.enums.MemberEnums;
import com.dl.member.param.UserBankQueryParam;
import com.dl.member.param.WithDrawParam;
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
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import javax.annotation.Resource;

@Service
@Transactional
@Slf4j
public class UserWithdrawService extends AbstractService<UserWithdraw> {
    @Resource
    private UserWithdrawMapper userWithdrawMapper;
    
    @Resource
    private IUserAccountService userAccountService;
    
    @Resource
    private IUserBankService userBankService;
    
    
    /**
     * 创建提现单
     * @param amount
     * @return
     */
     @Transactional
    public WithdrawalSnDTO saveWithdraw(UserWithdrawParam  userWithdrawParam){
    	Integer userId = SessionUtil.getUserId();
    	String withdrawalSn = SNGenerator.nextSN(SNBusinessCodeEnum.WITHDRAW_SN.getCode());
    	UserWithdraw userWithdraw = new UserWithdraw();
    	userWithdraw.setUserId(userId);
    	userWithdraw.setWithdrawalSn(withdrawalSn);
    	userWithdraw.setAmount(userWithdrawParam.getAmount());
    	Integer addTime = DateUtil.getCurrentTimeLong();
    	userWithdraw.setAddTime(addTime);
    	userWithdraw.setRealName(userWithdrawParam.getRealName());
    	userWithdraw.setCardNo(userWithdrawParam.getCardNo());
    	userWithdraw.setStatus(userWithdrawParam.getStatus());
    	String cardNo = userWithdrawParam.getCardNo();
    	UserBankQueryParam userBankQueryParam = new UserBankQueryParam();
    	userBankQueryParam.setBankCardCode(cardNo);
    	userBankQueryParam.setUserId(userId);
    	BaseResult<UserBankDTO> userBankRst = userBankService.queryUserBankByCondition(userBankQueryParam);
    	if(userBankRst.getCode() != 0) {
    		throw new ServiceException(MemberEnums.COMMON_ERROR.getcode(), "银行卡查询失败");
    	}
    	
    	UserBankDTO userBankDTO = userBankRst.getData();
    	userWithdraw.setBankName(userBankDTO.getBankName());
    	userWithdraw.setStatus(ProjectConstant.NOT_FINISH);
    	int rst = userWithdrawMapper.insertUserWithdraw(userWithdraw);
    	if(1 != rst) {
    		log.error("保存数据库提现单失败");
    		throw new ServiceException(MemberEnums.COMMON_ERROR.getcode(), "保存数据库提现单失败");
    	}
    	
    	WithdrawalSnDTO withdrawalSnDTO = new WithdrawalSnDTO();
    	withdrawalSnDTO.setWithdrawalSn(withdrawalSn);
    	withdrawalSnDTO.setAddTime(addTime);
		return  withdrawalSnDTO ;
    }
 
    
    
    /**
     * 根据提现单号查询提现单
     */
    public BaseResult<UserWithdraw> queryUserWithdraw(String withDrawSn){
    	UserWithdraw userWithdraw = new UserWithdraw();
    	userWithdraw.setWithdrawalSn(withDrawSn);
    	List<UserWithdraw> userWithdrawList = userWithdrawMapper.queryUserWithdrawBySelective(userWithdraw);
    	if(CollectionUtils.isEmpty(userWithdrawList)) {
    		return ResultGenerator.genResult(MemberEnums.DBDATA_IS_NULL.getcode(), "提现单不存在");
    	}
    	return ResultGenerator.genSuccessResult("查询提现单成功", userWithdrawList.get(0));
    }
    
    /**
     * 根据提现单号和userId查询提现单
     */
    public BaseResult<UserWithdrawDTO> queryUserWithdrawBySnAndUserId(String withDrawSn){
    	Integer userId = SessionUtil.getUserId();
    	UserWithdraw userWithdraw = new UserWithdraw();
    	userWithdraw.setWithdrawalSn(withDrawSn);
    	userWithdraw.setUserId(userId);
    	List<UserWithdraw> userWithdrawList = userWithdrawMapper.queryUserWithdrawBySelective(userWithdraw);
    	if(CollectionUtils.isEmpty(userWithdrawList)) {
    		return ResultGenerator.genResult(MemberEnums.DBDATA_IS_NULL.getcode(), "提现单不存在");
    	}
    	
    	UserWithdraw queryUserWithDraw = userWithdrawList.get(0);
    	UserWithdrawDTO userWithdrawDTO = new UserWithdrawDTO();
    	userWithdrawDTO.setStatus(queryUserWithDraw.getStatus());
    	userWithdrawDTO.setAmount(queryUserWithDraw.getAmount());
    	userWithdrawDTO.setWithdrawalSn(queryUserWithDraw.getWithdrawalSn());
    	return ResultGenerator.genSuccessResult("查询提现单成功", userWithdrawDTO);
    }
    
    /**
     * 根据accountId 查询提现单
     * @param accountId
     * @return
     */
    public BaseResult<UserWithdrawDTO> queryUserWithDrawByAccountId(Integer accountId){
    	UserWithdraw userWithdrawQuery =new UserWithdraw();
    	userWithdrawQuery.setAccountId(accountId);
    	List<UserWithdraw> userWithDrawList =  userWithdrawMapper.queryUserWithdrawBySelective(userWithdrawQuery);
    	if(CollectionUtils.isEmpty(userWithDrawList)) {
    		return ResultGenerator.genResult(MemberEnums.DBDATA_IS_NULL.getcode(), MemberEnums.DBDATA_IS_NULL.getMsg());
    	}
    	UserWithdrawDTO userWithDrawDTO = new UserWithdrawDTO();
    	BeanUtils.copyProperties(userWithDrawList.get(0), userWithDrawDTO);
		return ResultGenerator.genSuccessResult("查询提现单成功",userWithDrawDTO);
    }
    
    /**
     * 更新提现单
     * @param amount
     * @return
     */
    public BaseResult<String> updateWithdraw(UpdateUserWithdrawParam updateUserWithdrawParam){
    	String inPrams = JSON.toJSONString(updateUserWithdrawParam);
    	log.info(DateUtil.getCurrentDateTime()+"更新提现单参数:"+inPrams);
    	
    	BaseResult<UserWithdraw> userWithdrawRst = this.queryUserWithdraw(updateUserWithdrawParam.getWithdrawalSn());
    	if(userWithdrawRst.getCode() != 0) {
    		return ResultGenerator.genResult(userWithdrawRst.getCode(), userWithdrawRst.getMsg());
    	}
    	BigDecimal amount = userWithdrawRst.getData().getAmount();
    	
    	UserWithdraw userWithdraw = new UserWithdraw();
    	userWithdraw.setPaymentId(updateUserWithdrawParam.getPaymentId());
    	userWithdraw.setPayTime(updateUserWithdrawParam.getPayTime());
    	userWithdraw.setStatus(updateUserWithdrawParam.getStatus());
    	userWithdraw.setWithdrawalSn(updateUserWithdrawParam.getWithdrawalSn());
    	int rst = userWithdrawMapper.updateUserWithdrawBySelective(userWithdraw);
    	if(1 != rst) {
    		log.error("更新数据库提现单失败");
    		return ResultGenerator.genFailResult("更新数据库提现单失败");
    	}
    	
    	WithDrawParam withDrawParam = new WithDrawParam();
    	BaseResult<String> withdrawRst = userAccountService.withdrawUserMoney(withDrawParam);
    	
		return ResultGenerator.genSuccessResult("更新数据库提现单成功", withdrawRst.getData());
    }
    
}
