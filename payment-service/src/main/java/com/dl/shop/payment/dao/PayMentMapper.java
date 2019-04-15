package com.dl.shop.payment.dao;

import org.apache.ibatis.annotations.Param;

import com.dl.base.mapper.Mapper;
import com.dl.shop.payment.dto.PayFinishRedirectUrlTDTO;
import com.dl.shop.payment.model.PayMent;

public interface PayMentMapper extends Mapper<PayMent> {

	Integer selectJianLianConfig();

	PayFinishRedirectUrlTDTO selectRedirectInfo(@Param("channelId") String channelId);
}