package com.dl.shop.payment.service;
import com.dl.shop.payment.model.DlThirdApiLog;
import com.dl.shop.payment.dao.DlThirdApiLogMapper;
import com.dl.base.service.AbstractService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
public class DlThirdApiLogService extends AbstractService<DlThirdApiLog> {
    @Resource
    private DlThirdApiLogMapper dlThirdApiLogMapper;

}
