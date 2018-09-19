package com.dl.shop.payment.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dl.base.service.AbstractService;
import com.dl.shop.payment.dao.DlUserRealMapper;
import com.dl.shop.payment.model.DlUserReal;

@Service
@Transactional
public class DlUserRealService extends AbstractService<DlUserReal> {
	@Resource
	private DlUserRealMapper dlUserRealMapper;

	public DlUserReal findByUserId(Integer userId) {
		return dlUserRealMapper.findByUserId(userId);
	}
}
