/************************************************20170909  新增报表功能*************************************************/
#  sql script

DROP TABLE IF EXISTS `dl_user_withdraw_log`;
CREATE TABLE `dl_user_withdraw_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '提现进度ID',
  `withdraw_sn` varchar(40) NOT NULL COMMENT '提现单号',
  `log_time` int(11) NOT NULL COMMENT '执行时间',
  `log_code` tinyint(1) NOT NULL COMMENT '进度编号',
  `log_name` varchar(64) NOT NULL COMMENT '进度名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1443248 DEFAULT CHARSET=utf8 COMMENT='用户提现进度信息表';

DROP TABLE IF EXISTS `dl_withdraw_log`;
CREATE TABLE `dl_withdraw_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '提现进度ID',
  `log_code` tinyint(1) NOT NULL COMMENT '进度编号',
  `log_name` varchar(64) NOT NULL COMMENT '进度名称',
  `log_desc` varchar(256)  COMMENT '进度描述',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1443248 DEFAULT CHARSET=utf8 COMMENT='提现进度信息表';
