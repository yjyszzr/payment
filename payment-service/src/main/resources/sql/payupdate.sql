ALTER TABLE `dl_payment`
ADD COLUMN `is_readonly`  int(1) NULL COMMENT '是否固额 1：是  0：否' AFTER `read_money`,
ADD COLUMN `read_money`  varchar(200) NULL COMMENT '固定额度；从小到大以“;”分割' AFTER `is_readonly_`;
INSERT INTO `dl_payment` VALUES ('12', 'app_lidpay', '支付宝', '1', null, '支付宝', '0', '255', '1', null, 'https://szcq-icon.oss-cn-beijing.aliyuncs.com/zfb20190413.png', '0', '');
INSERT INTO `dl_payment` VALUES ('13', 'app_apay', '艾支付', '1', null, '艾支付', '0', '255', '0', null, 'https://szcq-icon.oss-cn-beijing.aliyuncs.com/zfb20190413.png', '0', null);
INSERT INTO `dl_payment` VALUES ('14', 'app_awx', '艾支付微信', '1', null, '艾支付微信', '0', '255', '0', null, 'https://szcq-icon.oss-cn-beijing.aliyuncs.com/weixin.png', '1', '10;20;30;50;100;200;300');


                                      腾迅云-圣和彩店-关键配置

eureka  172.30.0.6

datasource 39.107.102.22
username	cxm_user_rw
password	YNShTBmL1X1X

redis	172.30.0.6               
圣和彩店 生产环境IP 94.191.113.169      