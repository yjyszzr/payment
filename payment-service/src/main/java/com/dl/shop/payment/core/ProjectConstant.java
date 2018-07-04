package com.dl.shop.payment.core;

public class ProjectConstant {

	public static final String BASE_PACKAGE = "com.dl.shop.payment";//项目基础包名称，根据自己公司的项目修改

    public static final String MODEL_PACKAGE = BASE_PACKAGE + ".model";//Model所在包
    public static final String MAPPER_PACKAGE = BASE_PACKAGE + ".dao";//Mapper所在包
    public static final String SERVICE_PACKAGE = BASE_PACKAGE + ".service";//Service所在包
    public static final String SERVICE_IMPL_PACKAGE = SERVICE_PACKAGE + ".impl";//ServiceImpl所在包
    public static final String CONTROLLER_PACKAGE = BASE_PACKAGE + ".web";//Controller所在包

    public static final String MAPPER_INTERFACE_REFERENCE = BASE_PACKAGE + ".mapper.Mapper";//Mapper插件基础接口的完全限定名
    public static final String MAPPER_BASE = "com.dl.base.mapper.Mapper";//Mapper插件基础接口的完全限定名
    
    
    public static final String NOT_FINISH = "0";// 用户未完成
    public static final String FINISH = "1";//用户已完成
    public static final String FAILURE = "2";//失败
    
    public static final Integer REWARD = 1;//奖金
    public static final Integer RECHARGE = 2;//充值
    public static final Integer BUY = 3;//购彩
    public static final Integer WITHDRAW = 4;//提现
    public static final Integer BONUS =5;//红包
    public static final Integer ACCOUNT_ROLLBACK =6;//账户回滚
    public static final Integer REFOUND =7;
      
    public static final String STATUS_FAIL_REFUNDING = "4";
    public static final String STATUS_BANK_APPROVING = "3";
    public static final String STATUS_FAILURE = "2";
	public static final String STATUS_SUCC = "1";
	public static final String STATUS_UNCOMPLETE = "0";
	
	// 0-未支付;1-已支付;2-已取消;3-支付失败;4-已退款
	public static final Integer IS_PAID_NOT_FINISH = 0;
	public static final Integer IS_PAID_FINISH = 1;
	public static final Integer IS_PAID_CANCLE = 2;
	public static final Integer IS_PAID_FAILURE = 3;
	public static final Integer IS_PAID_REFOUND = 4;
	
	//是否充过值  1- 充过值 0- 未充过值
	public static final String hasRecharged = "1";
	public static final String noRecharged = "0";
	
	//通用 的代表 否-0 是 -1
	public static final Integer NO_0 = 0;
	public static final Integer YES_1 = 1;
	
	
	
	
}
