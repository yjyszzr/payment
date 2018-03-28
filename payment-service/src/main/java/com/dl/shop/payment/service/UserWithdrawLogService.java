package com.dl.shop.payment.service;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dl.base.service.AbstractService;
import com.dl.shop.payment.dao.UserWithdrawLogMapper;
import com.dl.shop.payment.dao.WithdrawLogMapper;
import com.dl.shop.payment.dto.UserWithdrawLogDTO;
import com.dl.shop.payment.model.UserWithdrawLog;
import com.dl.shop.payment.model.WithdrawLog;

@Service
@Transactional
public class UserWithdrawLogService extends AbstractService<UserWithdrawLog> {
    @Resource
    private UserWithdrawLogMapper userWithdrawLogMapper;
    @Resource
    private WithdrawLogMapper withdrawLogMapper;

    
	public List<UserWithdrawLogDTO> findByWithdrawSn(String withdawSn) {
		List<WithdrawLog> withdrawLogs = withdrawLogMapper.selectAll();
		List<UserWithdrawLog> logs = userWithdrawLogMapper.findByWithdrawSn(withdawSn);
		return withdrawLogs.stream().map(log->{
			Integer logCode = log.getLogCode();
			UserWithdrawLogDTO dto = new UserWithdrawLogDTO();
			dto.setLogCode(log.getLogCode());
			dto.setLogName(log.getLogName());
			for(UserWithdrawLog uwLog: logs) {
				if(uwLog.getLogCode() == logCode) {
					dto.setWithdrawSn(uwLog.getWithdrawSn());
					LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(uwLog.getLogTime(), 0, ZoneOffset.UTC);
					dto.setLogTime(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
					logs.remove(uwLog);
				}
			}
			return dto;
		}).sorted((item1,item2)->{
			return item2.getLogCode().compareTo(item1.getLogCode());
		}).collect(Collectors.toList());
	}

}
