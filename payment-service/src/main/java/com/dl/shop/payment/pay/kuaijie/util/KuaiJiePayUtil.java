package com.dl.shop.payment.pay.kuaijie.util;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.dl.shop.payment.pay.kuaijie.config.KuaiJiePayConfig;
import com.dl.shop.payment.pay.kuaijie.entity.KuaiJiePayNotifyEntity;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KuaiJiePayUtil {
	@Resource
	private KuaiJiePayConfig kuaiJiePayConfig;

	public Boolean booleanCheckSign(KuaiJiePayNotifyEntity kuaiJiePayNotifyEntity) {
		return null;
	}
	
}
