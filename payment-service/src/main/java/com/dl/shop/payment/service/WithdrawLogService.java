package com.dl.shop.payment.service;
import com.dl.shop.payment.model.WithdrawLog;
import com.dl.shop.payment.dao.WithdrawLogMapper;
import com.dl.base.service.AbstractService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
public class WithdrawLogService extends AbstractService<WithdrawLog> {
    @Resource
    private WithdrawLogMapper withdrawLogMapper;

}
