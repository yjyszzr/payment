package com.dl.shop.payment.schedul;

import javax.annotation.Resource;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.dl.order.api.IOrderService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableScheduling
public class PaySchedul {
	
	/**
	 * 处理订单支付超时的定时任务
	 */
	@Scheduled(cron = "0 0/5 * * * ?")
    public void dealWithNotPayAndBeyondTimeOrder() {
		log.info("开始执行处理订单支付超时的定时任务");
		dealWithNotPayAndBeyondTimeOrder();
		log.info("结束执行处理订单支付超时的定时任务");
	}

}
