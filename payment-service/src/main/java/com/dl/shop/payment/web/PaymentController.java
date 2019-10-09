package com.dl.shop.payment.web;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dl.activity.api.IActiviService;
import com.dl.base.enums.SNBusinessCodeEnum;
import com.dl.base.model.UserDeviceInfo;
import com.dl.base.param.EmptyParam;
import com.dl.base.result.BaseResult;
import com.dl.base.result.ResultGenerator;
import com.dl.base.util.DateUtil;
import com.dl.base.util.JSONHelper;
import com.dl.base.util.MD5Util;
import com.dl.base.util.SNGenerator;
import com.dl.base.util.SessionUtil;
import com.dl.lottery.api.ILotteryPrintService;
import com.dl.lottery.enums.LotteryResultEnum;
import com.dl.lotto.enums.LottoResultEnum;
import com.dl.member.api.IActivityService;
import com.dl.member.api.ISysConfigService;
import com.dl.member.api.IUserAccountService;
import com.dl.member.api.IUserBankService;
import com.dl.member.api.IUserBonusService;
import com.dl.member.api.IUserMessageService;
import com.dl.member.api.IUserService;
import com.dl.member.dto.RechargeDataActivityDTO;
import com.dl.member.dto.SurplusPaymentCallbackDTO;
import com.dl.member.dto.SysConfigDTO;
import com.dl.member.dto.UserBankDTO;
import com.dl.member.dto.UserBonusDTO;
import com.dl.member.dto.UserDTO;
import com.dl.member.dto.UserRealDTO;
import com.dl.member.param.BonusLimitConditionParam;
import com.dl.member.param.IDParam;
import com.dl.member.param.StrParam;
import com.dl.member.param.SurplusPayParam;
import com.dl.member.param.SysConfigParam;
import com.dl.member.param.UpdateUserRechargeParam;
import com.dl.member.param.UserBankQueryParam;
import com.dl.member.param.UserBonusIdParam;
import com.dl.member.param.UserIdRealParam;
import com.dl.order.api.IOrderService;
import com.dl.order.dto.OrderDTO;
import com.dl.order.param.SubmitOrderParam;
import com.dl.order.param.SubmitOrderParam.TicketDetail;
import com.dl.order.param.UpdateOrderInfoParam;
import com.dl.order.param.UpdateOrderPayStatusParam;
import com.dl.shop.payment.core.ProjectConstant;
import com.dl.shop.payment.dao.DlPayQrBase64Mapper;
import com.dl.shop.payment.dto.PayLogDTO;
import com.dl.shop.payment.dto.PayLogDetailDTO;
import com.dl.shop.payment.dto.PayReturnDTO;
import com.dl.shop.payment.dto.PayWaysDTO;
import com.dl.shop.payment.dto.PaymentDTO;
import com.dl.shop.payment.dto.PriceDTO;
import com.dl.shop.payment.dto.RechargeUserDTO;
import com.dl.shop.payment.dto.RspOrderQueryDTO;
import com.dl.shop.payment.dto.UserBetDetailInfoDTO;
import com.dl.shop.payment.dto.UserBetPayInfoDTO;
import com.dl.shop.payment.dto.UserGoPayInfoDTO;
import com.dl.shop.payment.dto.ValidPayDTO;
import com.dl.shop.payment.enums.PayEnums;
import com.dl.shop.payment.model.DlPayQrBase64;
import com.dl.shop.payment.model.PayLog;
import com.dl.shop.payment.model.UnifiedOrderParam;
import com.dl.shop.payment.model.UserWithdrawLog;
import com.dl.shop.payment.param.AllPaymentInfoParam;
import com.dl.shop.payment.param.GoPayBeforeParam;
import com.dl.shop.payment.param.GoPayParam;
import com.dl.shop.payment.param.PayLogIdParam;
import com.dl.shop.payment.param.PayLogOrderSnParam;
import com.dl.shop.payment.param.RechargeParam;
import com.dl.shop.payment.param.ReqOrderQueryParam;
import com.dl.shop.payment.param.RollbackOrderAmountParam;
import com.dl.shop.payment.param.RollbackThirdOrderAmountParam;
import com.dl.shop.payment.param.UrlBase64Param;
import com.dl.shop.payment.param.UserIdParam;
import com.dl.shop.payment.param.WithdrawParam;
import com.dl.shop.payment.pay.common.RspOrderQueryEntity;
import com.dl.shop.payment.pay.jhpay.util.HttpConfig;
import com.dl.shop.payment.pay.rongbao.config.ReapalH5Config;
import com.dl.shop.payment.pay.rongbao.demo.RongUtil;
import com.dl.shop.payment.pay.xianfeng.util.XianFengPayUtil;
import com.dl.shop.payment.pay.yinhe.config.ConfigerPay;
import com.dl.shop.payment.pay.yinhe.entity.RspYinHeEntity;
import com.dl.shop.payment.pay.yinhe.util.PayUtil;
import com.dl.shop.payment.pay.yinhe.util.YinHeUtil;
import com.dl.shop.payment.service.APayService;
import com.dl.shop.payment.service.JhPayService;
import com.dl.shop.payment.service.LidPayService;
import com.dl.shop.payment.service.PayLogService;
import com.dl.shop.payment.service.PayMentService;
import com.dl.shop.payment.service.RkPayService;
import com.dl.shop.payment.service.SmkPayService;
import com.dl.shop.payment.service.UbeyPayService;
import com.dl.shop.payment.service.UserRechargeService;
import com.dl.shop.payment.service.UserWithdrawLogService;
import com.dl.shop.payment.service.YunPayService;
import com.dl.shop.payment.utils.DateUtilPay;
import com.dl.shop.payment.utils.QrUtil;
import com.dl.shop.payment.utils.WxpayUtil;
import com.dl.store.api.IStoreUserMoneyService;
import com.dl.store.param.FirstPayTimeParam;
import com.github.pagehelper.util.StringUtil;

import io.swagger.annotations.ApiOperation;
import net.sf.json.util.JSONUtils;

@Controller
@RequestMapping("/payment")
public class PaymentController extends AbstractBaseController {
	private static Boolean CHECKORDER_TASKRUN = Boolean.FALSE;
	private static Boolean CHECKRECHARGE_TASKRUN = Boolean.FALSE;
	private final static Logger logger = LoggerFactory
			.getLogger(PaymentController.class);
	@Resource
	private PayLogService payLogService;
	@Resource
	private IActiviService activiService;
	@Resource
	private PayMentService paymentService;
	@Resource
	private UbeyPayService ubeyPayService;
	@Resource
	private LidPayService lidPayService;
	@Resource
	private YunPayService yunPayService;
	@Resource
	private APayService aPayService;
	@Resource
	private RkPayService rkPayService;
	@Resource
	private SmkPayService smkPayService;
	@Resource
	private WxpayUtil wxpayUtil;
	@Resource
	private YinHeUtil yinHeUtil;
	@Resource
	private JhPayService jhpayService;
	@Autowired
	private IUserAccountService userAccountService;
	@Autowired
	private IOrderService orderService;
	@Autowired
	private IUserBankService userBankService;
	@Resource
	private StringRedisTemplate stringRedisTemplate;
	@Resource
	private UserWithdrawLogService userWithdrawLogService;
	@Resource
	private IUserMessageService userMessageService;
	@Resource
	private IUserService userService;
	@Resource
	private ILotteryPrintService lotteryPrintService;
	@Resource
	private UserRechargeService userRechargeService;
	@Resource
	private PayUtil payUtil;
	@Resource
	private ConfigerPay cfgPay;
	@Resource
	private ReapalH5Config rongCfg;
	@Resource
	private RongUtil rongUtil;
	@Resource
	private XianFengPayUtil xianFengUtil;
	@Resource
	private IActivityService activityService;
	@Resource
	private IUserBonusService userBonusService;
	@Resource
	private DlPayQrBase64Mapper dlPayQrBase64Mapper;
	@Value("${yinhe.app_H5_qr_url}")
	private String appH5QrUrl;
	@Value("${yinhe.app_ZFB_H5_qr_url}")
	private String appZFBH5QrUrl;
	@Resource
	private ISysConfigService iSysConfigService;

	@Resource
	private IStoreUserMoneyService iStoreUserMoneyService;

	@ApiOperation(value = "支付宝授权", notes = "支付宝授权")
	@GetMapping("/payAuthoriz")
	@ResponseBody
	public BaseResult<?> payAuthoriz(HttpServletRequest request) {
		logger.info("payAuthoriz========app_id========"
				+ request.getParameter("app_id"));
		String userId = HttpConfig.getUserid(request.getParameter("app_id"),
				request.getParameter("auth_code"));
		logger.info("payAuthoriz========userId========" + userId);
		if (com.alibaba.druid.util.StringUtils.isEmpty(userId)) {
			return ResultGenerator.genFailResult("支付宝应用授权失败。");
		}
		logger.info("payAuthoriz========pay_type========"
				+ request.getParameter("payType"));
		logger.info("payAuthoriz========orderSn========"
				+ request.getParameter("orderSn"));
		String pay_type = request.getParameter("payType");
		if ("zf".equals(pay_type)) {
			logger.info("payAuthoriz========支付userId========" + userId);
			GoPayParam param = new GoPayParam();
			param.setOrderSn(request.getParameter("orderSn"));
			param.setUserId(userId);
			param.setPayCode("app_jhpay");
			return this.nUnifiedOrderNew(param, request);
		} else if ("cz".equals(pay_type)) {
			logger.info("payAuthoriz========充值userId========" + userId);
			RechargeParam param = new RechargeParam();
			param.setOrderSn(request.getParameter("orderSn"));
			param.setUserId(userId);
			param.setPayCode("app_jhpay");
			return this.rechargeForAppNew(param, request);
		} else {
			return ResultGenerator.genFailResult("参数错误");
		}
	}

	@ApiOperation(value = "系统可用第三方支付方式", notes = "系统可用第三方支付方式")
	@PostMapping("/allPayment")
	@ResponseBody
	public BaseResult<List<PaymentDTO>> allPaymentInfo(
			@RequestBody AllPaymentInfoParam param) {
		String deviceUnique = "";
		UserDeviceInfo userDevice = SessionUtil.getUserDevice();
		if ("android".equals(userDevice.getPlat())) {
			deviceUnique = userDevice.getAndroidid();
		} else if ("iphone".equals(userDevice.getPlat())) {
			deviceUnique = userDevice.getIDFA();
		} else if ("h5".equals(userDevice.getPlat())) {
			deviceUnique = "h5";
		}
		List<PaymentDTO> list = paymentService.findAllDto(deviceUnique);
		if ("1.3.0".compareTo(userDevice.getAppv()) > 0) {// 1.3.0以前版本隐藏聚合支付
			list = list.stream().filter(dto -> {
				if ("app_jhpay".equalsIgnoreCase(dto.getPayCode())
						|| "app_yunpay".equalsIgnoreCase(dto.getPayCode())) {
					return false;
				}
				return true;
			}).collect(Collectors.toList());
		}
		return ResultGenerator.genSuccessResult("success", list);
	}

	@ApiOperation(value = "线下支付订单生成", notes = "系统可用第三方支付方式")
	@PostMapping("/appOfflineCreateOrder")
	@ResponseBody
	public BaseResult<String> appOfflineCreateOrder(
			@RequestBody AllPaymentInfoParam param) {
		String payToken = param.getPayToken();
		// 如果支付方式为【线下支付】并且【订单号为空】则生成订单
		if (param.getPayCode() != null
				&& "app_offline".equals(param.getPayCode())
				&& StringUtil.isEmpty(param.getOrderSn())) {
			logger.info("appOfflineCreateOrder METHOD:=" + param.getPayCode()
					+ "^^^^^^^^^" + param.getOrderSn());
			// 校验payToken的有效性
			String jsonData = stringRedisTemplate.opsForValue().get(payToken);
			if (StringUtils.isBlank(jsonData)) {
				logger.info("订单信息获取为空！");
				return ResultGenerator.genResult(
						PayEnums.PAY_TOKEN_EXPRIED.getcode(),
						PayEnums.PAY_TOKEN_EXPRIED.getMsg());
			}
			// 清除payToken
			// stringRedisTemplate.delete(payToken);

			UserBetPayInfoDTO dto = null;
			try {
				dto = JSONHelper.getSingleBean(jsonData,
						UserBetPayInfoDTO.class);
			} catch (Exception e1) {
				logger.error("支付信息转DIZQUserBetInfoDTO对象失败！", e1);
				return ResultGenerator.genFailResult("订单信息异常，创建失败！");
			}
			if (null == dto) {
				return ResultGenerator.genFailResult("订单信息异常，创建失败！");
			}
			Integer userId = dto.getUserId();
			Integer currentId = SessionUtil.getUserId();
			if (!userId.equals(currentId)) {
				logger.info("支付信息不是当前用户的待支付彩票！");
				return ResultGenerator.genFailResult("订单信息异常，创建失败！");
			}
			Double orderMoney = dto.getOrderMoney();
			Integer userBonusId = 0;// form
			BigDecimal ticketAmount = BigDecimal.valueOf(orderMoney);// from
			BigDecimal bonusAmount = BigDecimal.valueOf(dto.getBonusAmount());// from
																				// paytoken
			BigDecimal moneyPaid = BigDecimal
					.valueOf(orderMoney - dto.getBonusAmount());
			BigDecimal surplus = BigDecimal.valueOf(dto.getSurplus());// from
			BigDecimal thirdPartyPaid = BigDecimal
					.valueOf(dto.getThirdPartyPaid());

			// 比赛提前1h 禁止支付
			Integer sysLimitBetTime = 3600;
			SysConfigParam sysConfigParam = new SysConfigParam();
			sysConfigParam.setBusinessId(4);
			BaseResult<SysConfigDTO> sysConfigDTOBaseResult = iSysConfigService
					.querySysConfig(sysConfigParam);
			if (sysConfigDTOBaseResult.isSuccess()) {
				sysLimitBetTime = sysConfigDTOBaseResult.getData().getValue()
						.intValue();
			}
			List<UserBetDetailInfoDTO> userBetCellInfos = dto
					.getBetDetailInfos();
			UserBetDetailInfoDTO min = userBetCellInfos.get(0);
			if (userBetCellInfos.size() > 1) {
				min = userBetCellInfos.stream().min((cell1,
						cell2) -> cell1.getMatchTime() - cell2.getMatchTime())
						.get();
			}
			String strMatchTime = DateUtil.getTimeString(min.getMatchTime(),
					DateUtil.datetimeFormat);
			String strNowTime = DateUtil.getTimeString(
					DateUtil.getCurrentTimeLong(), DateUtil.datetimeFormat);

			String seconds = DateUtilPay.dateSubtractionHours(strNowTime,
					strMatchTime);
			logger.info("nUnifiedOrder()：提前售票： 比赛时间=" + min.getMatchTime()
					+ "||" + strMatchTime + "提前时间=" + sysLimitBetTime + "当前时间="
					+ DateUtil.getCurrentTimeLong() + "||" + strNowTime);
			logger.info("nUnifiedOrder()：提前售票： 是否停止售票="
					+ (Integer.valueOf(seconds) <= sysLimitBetTime));
			if (Integer.valueOf(seconds) <= sysLimitBetTime) {
				return ResultGenerator.genResult(
						LotteryResultEnum.BET_TIME_LIMIT.getCode(),
						LotteryResultEnum.BET_TIME_LIMIT.getMsg());
			}

			List<TicketDetail> ticketDetails = userBetCellInfos.stream()
					.map(betCell -> {
						TicketDetail ticketDetail = new TicketDetail();
						ticketDetail.setMatch_id(betCell.getMatchId());
						ticketDetail.setChangci(betCell.getChangci());
						int matchTime = betCell.getMatchTime();
						if (matchTime > 0) {
							ticketDetail.setMatchTime(Date
									.from(Instant.ofEpochSecond(matchTime)));
						}
						ticketDetail.setMatchTeam(betCell.getMatchTeam());
						ticketDetail.setLotteryClassifyId(
								betCell.getLotteryClassifyId());
						ticketDetail.setLotteryPlayClassifyId(
								betCell.getLotteryPlayClassifyId());
						ticketDetail.setTicketData(betCell.getTicketData());
						ticketDetail.setIsDan(betCell.getIsDan());
						ticketDetail.setIssue(betCell.getPlayCode());
						ticketDetail.setFixedodds(betCell.getFixedodds());
						ticketDetail.setBetType(betCell.getBetType());
						return ticketDetail;
					}).collect(Collectors.toList());

			// order生成
			SubmitOrderParam submitOrderParam = new SubmitOrderParam();
			submitOrderParam.setTicketNum(dto.getTicketNum());
			submitOrderParam.setMoneyPaid(moneyPaid);
			submitOrderParam.setTicketAmount(ticketAmount);
			submitOrderParam.setSurplus(surplus);
			submitOrderParam.setThirdPartyPaid(thirdPartyPaid);
			submitOrderParam.setPayName("线下支付");
			submitOrderParam.setPayCode("app_offline");
			submitOrderParam.setUserBonusId(userBonusId);
			submitOrderParam.setBonusAmount(bonusAmount);
			submitOrderParam.setOrderFrom(dto.getRequestFrom());
			int lotteryClassifyId = dto.getLotteryClassifyId();
			submitOrderParam.setLotteryClassifyId(lotteryClassifyId);
			int lotteryPlayClassifyId = dto.getLotteryPlayClassifyId();
			submitOrderParam.setLotteryPlayClassifyId(lotteryPlayClassifyId);
			submitOrderParam.setPassType(dto.getBetType());
			submitOrderParam.setPlayType("0" + dto.getPlayType());
			submitOrderParam.setBetNum(dto.getBetNum());
			submitOrderParam.setPlayTypeDetail(dto.getPlayTypeDetail());
			submitOrderParam.setCathectic(dto.getTimes());
			if (lotteryPlayClassifyId != 8 && lotteryClassifyId == 1) {
				if (ticketDetails.size() > 1) {
					Optional<TicketDetail> max = ticketDetails.stream()
							.max((detail1, detail2) -> detail1.getMatchTime()
									.compareTo(detail2.getMatchTime()));
					submitOrderParam.setMatchTime(max.get().getMatchTime());
				} else {
					submitOrderParam
							.setMatchTime(ticketDetails.get(0).getMatchTime());
				}
			}
			submitOrderParam.setForecastMoney(dto.getForecastMoney());

			submitOrderParam.setIssue(dto.getIssue());
			submitOrderParam.setTicketDetails(ticketDetails);
			submitOrderParam.setPayCode(param.getPayCode());
			submitOrderParam.setPayToken(payToken);
			BaseResult<OrderDTO> createOrder = orderService
					.createOrder(submitOrderParam);
			if (createOrder.getCode() != 0) {
				logger.info("订单创建失败！");
				return ResultGenerator.genFailResult("订单创建失败！");
			}

		}
		return ResultGenerator.genSuccessResult("success", "订单创建成功");
	}

	@ApiOperation(value = "系统可用第三方支付方式带有了充值活动信息", notes = "系统可用第三方支付方式")
	@PostMapping("/allPaymentWithRecharge")
	@ResponseBody
	public BaseResult<PayWaysDTO> allPaymentInfoWithRecharge(
			@RequestBody AllPaymentInfoParam param) {
		String deviceUnique = "";
		UserDeviceInfo userDevice = SessionUtil.getUserDevice();
		if ("android".equals(userDevice.getPlat())) {
			deviceUnique = userDevice.getAndroidid();
		} else if ("iphone".equals(userDevice.getPlat())) {
			deviceUnique = userDevice.getIDFA();
		} else if ("h5".equals(userDevice.getPlat())) {
			deviceUnique = "h5";
		}
		PayWaysDTO payWaysDTO = new PayWaysDTO();
		List<PaymentDTO> paymentDTOList = paymentService
				.findAllDto(deviceUnique);

		if ("1.3.0".compareTo(userDevice.getAppv()) > 0) {// 1.3.0以前版本隐藏聚合支付
			paymentDTOList = paymentDTOList.stream().filter(dto -> {
				if ("app_jhpay".equalsIgnoreCase(dto.getPayCode())
						|| "app_yunpay".equalsIgnoreCase(dto.getPayCode())) {
					return false;
				}
				return true;
			}).collect(Collectors.toList());
		}

		payWaysDTO.setPaymentDTOList(paymentDTOList);

		StrParam strParam = new StrParam();
		BaseResult<RechargeDataActivityDTO> rechargeActRst = activityService
				.queryValidRechargeActivity(strParam);
		if (rechargeActRst.getCode() != 0) {
			payWaysDTO.setIsHaveRechargeAct(0);
		}

		RechargeDataActivityDTO rechargeDataActivityDTO = rechargeActRst
				.getData();
		payWaysDTO.setIsHaveRechargeAct(
				rechargeDataActivityDTO.getIsHaveRechargeAct());

		Integer userId = SessionUtil.getUserId();
		RechargeUserDTO rechargeUserDTO = userRechargeService
				.createRechargeUserDTO(userId);
		payWaysDTO.setRechargeUserDTO(rechargeUserDTO);

		return ResultGenerator.genSuccessResult("success", payWaysDTO);
	}

	@ApiOperation(value = "用户支付回退接口", notes = "")
	@PostMapping("/rollbackOrderAmount")
	@ResponseBody
	public BaseResult<?> rollbackOrderAmount(
			@RequestBody RollbackOrderAmountParam param) {
		if (param.getAmt() == null || param.getAmt().floatValue() <= 0) {
			return ResultGenerator.genFailResult("请传入合法金额");
		}
		if (StringUtils.isEmpty(param.getOrderSn())) {
			return ResultGenerator.genFailResult("请传入合法订单号");
		}
		return paymentService.rollbackOrderAmount(param);
	}

	@ApiOperation(value = "获取支付base64", notes = "")
	@PostMapping("/urlBase64")
	@ResponseBody
	public BaseResult<?> urlBase64(@RequestBody UrlBase64Param param) {
		if (param == null || param.getBase64Id() == null) {
			return ResultGenerator.genFailResult("请传入合法base64Id");
		}
		DlPayQrBase64 dlPayQrBase64 = dlPayQrBase64Mapper
				.selectDlPayQrBase64ById(param.getBase64Id());
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("base64Url", dlPayQrBase64.getBase64Content());
		return ResultGenerator.genSuccessResult("", resultMap);
	}

	@ApiOperation(value = "手动操作第三方退款接口", notes = "")
	@PostMapping("/rollbackAmountThird")
	@ResponseBody
	public BaseResult<?> rollbackAmomtThird(
			@RequestBody RollbackThirdOrderAmountParam param) {
		logger.info("[rollbackAmomtThird]" + " 手工退款操作:" + param.getOrderSn());
		return paymentService.rollbackAmountThird(param);
	}

	@ApiOperation(value = "app支付调用", notes = "payToken:商品中心购买信息保存后的返回值 ，payCode：支付编码，app端微信支付为app_weixin")
	@PostMapping("/app")
	@ResponseBody
	public BaseResult<PayReturnDTO> unifiedOrderForApp(
			@RequestBody GoPayParam param, HttpServletRequest request) {
		// 20181203 加入提示
		return ResultGenerator.genResult(PayEnums.PAY_STOP_SERVICE.getcode(),
				PayEnums.PAY_STOP_SERVICE.getMsg());

		/*
		 * String loggerId = "payment_app_" + System.currentTimeMillis();
		 * logger.info(loggerId + " int /payment/app, userId=" +
		 * SessionUtil.getUserId() + " ,payCode=" + param.getPayCode()); String
		 * payToken = param.getPayToken(); if (StringUtils.isBlank(payToken)) {
		 * logger.info(loggerId + "payToken值为空！"); return
		 * ResultGenerator.genResult(PayEnums.PAY_TOKEN_EMPTY.getcode(),
		 * PayEnums.PAY_TOKEN_EMPTY.getMsg()); } // 校验payToken的有效性 String
		 * jsonData = stringRedisTemplate.opsForValue().get(payToken); if
		 * (StringUtils.isBlank(jsonData)) { logger.info(loggerId +
		 * "支付信息获取为空！"); return
		 * ResultGenerator.genResult(PayEnums.PAY_TOKEN_EXPRIED.getcode(),
		 * PayEnums.PAY_TOKEN_EXPRIED.getMsg()); } //ubey检验最小金额
		 * if("app_ubey".equals(param.getPayCode())) { boolean check =
		 * ubeyPayService.checkAmount(jsonData); if(check) { return
		 * ResultGenerator.genFailResult("该支付方式最低消费1元 "); } } // 清除payToken
		 * stringRedisTemplate.delete(payToken);
		 * 
		 * DIZQUserBetInfoDTO dto = null; try { dto =
		 * JSONHelper.getSingleBean(jsonData, DIZQUserBetInfoDTO.class); } catch
		 * (Exception e1) { logger.error(loggerId +
		 * "支付信息转DIZQUserBetInfoDTO对象失败！", e1); return
		 * ResultGenerator.genFailResult("支付信息异常，支付失败！"); } if (null == dto) {
		 * return ResultGenerator.genFailResult("支付信息异常，支付失败！"); }
		 * 
		 * Integer userId = dto.getUserId(); Integer currentId =
		 * SessionUtil.getUserId(); if (!userId.equals(currentId)) {
		 * logger.info(loggerId + "支付信息不是当前用户的待支付彩票！"); return
		 * ResultGenerator.genFailResult("支付信息异常，支付失败！"); } Integer userBonusId
		 * = StringUtils.isBlank(dto.getBonusId()) ? 0 :
		 * Integer.valueOf(dto.getBonusId());// form // paytoken BigDecimal
		 * ticketAmount = BigDecimal.valueOf(dto.getMoney());// from // paytoken
		 * BigDecimal bonusAmount = BigDecimal.valueOf(dto.getBonusAmount());//
		 * from // paytoken BigDecimal moneyPaid =
		 * BigDecimal.valueOf(dto.getMoney() - dto.getBonusAmount()); ;// from
		 * paytoken BigDecimal surplus = BigDecimal.valueOf(dto.getSurplus());//
		 * from // paytoken BigDecimal thirdPartyPaid =
		 * BigDecimal.valueOf(dto.getThirdPartyPaid());
		 * List<DIZQUserBetCellInfoDTO> userBetCellInfos =
		 * dto.getUserBetCellInfos(); final String betType = dto.getBetType();
		 * List<TicketDetail> ticketDetails =
		 * userBetCellInfos.stream().map(betCell -> { TicketDetail ticketDetail
		 * = new TicketDetail(); ticketDetail.setMatch_id(betCell.getMatchId());
		 * ticketDetail.setChangci(betCell.getChangci()); int matchTime =
		 * betCell.getMatchTime(); if (matchTime > 0) {
		 * ticketDetail.setMatchTime(Date.from(Instant.ofEpochSecond(matchTime))
		 * ); } ticketDetail.setMatchTeam(betCell.getMatchTeam());
		 * ticketDetail.setLotteryClassifyId(betCell.getLotteryClassifyId());
		 * ticketDetail.setLotteryPlayClassifyId(betCell.
		 * getLotteryPlayClassifyId());
		 * ticketDetail.setTicketData(betCell.getTicketData());
		 * ticketDetail.setIsDan(betCell.getIsDan());
		 * ticketDetail.setIssue(betCell.getPlayCode());
		 * ticketDetail.setFixedodds(betCell.getFixedodds());
		 * ticketDetail.setBetType(betType); return ticketDetail;
		 * }).collect(Collectors.toList()); // 余额支付 boolean hasSurplus = false;
		 * if ((surplus != null && surplus.doubleValue() > 0) || (bonusAmount !=
		 * null && bonusAmount.doubleValue() > 0)) { hasSurplus = true; } //
		 * 临时添加 boolean isSurplus = false; if (surplus != null &&
		 * surplus.doubleValue() > 0) { isSurplus = true; } // 第三方支付 boolean
		 * hasThird = false; if (thirdPartyPaid != null &&
		 * thirdPartyPaid.doubleValue() > 0) { hasThird = true; String payCode =
		 * param.getPayCode(); if (StringUtils.isBlank(payCode)) {
		 * logger.info(loggerId + "第三方支付，paycode为空~"); return
		 * ResultGenerator.genResult(PayEnums.PAY_CODE_BLANK.getcode(),
		 * PayEnums.PAY_CODE_BLANK.getMsg()); } } PaymentDTO paymentDto = null;
		 * String payName = null; if (hasThird) { // 支付方式校验 String payCode =
		 * param.getPayCode(); if (StringUtils.isBlank(payCode)) {
		 * logger.info(loggerId + "订单第三支付没有提供paycode！"); return
		 * ResultGenerator.genFailResult("对不起，您还没有选择第三方支付！", null); }
		 * BaseResult<PaymentDTO> paymentResult =
		 * paymentService.queryByCode(payCode); if (paymentResult.getCode() !=
		 * 0) { logger.info(loggerId + "订单第三方支付提供paycode有误！payCode=" + payCode);
		 * return ResultGenerator.genFailResult("请选择有效的支付方式！", null); }
		 * paymentDto = paymentResult.getData(); payName =
		 * paymentDto.getPayName(); } // order生成 SubmitOrderParam
		 * submitOrderParam = new SubmitOrderParam();
		 * submitOrderParam.setTicketNum(dto.getTicketNum());
		 * submitOrderParam.setMoneyPaid(moneyPaid);
		 * submitOrderParam.setTicketAmount(ticketAmount);
		 * submitOrderParam.setSurplus(surplus);
		 * submitOrderParam.setThirdPartyPaid(thirdPartyPaid);
		 * submitOrderParam.setPayName(payName);
		 * submitOrderParam.setUserBonusId(userBonusId);
		 * submitOrderParam.setBonusAmount(bonusAmount);
		 * submitOrderParam.setOrderFrom(dto.getRequestFrom()); int
		 * lotteryClassifyId = dto.getLotteryClassifyId();
		 * submitOrderParam.setLotteryClassifyId(lotteryClassifyId); int
		 * lotteryPlayClassifyId = dto.getLotteryPlayClassifyId();
		 * submitOrderParam.setLotteryPlayClassifyId(lotteryPlayClassifyId);
		 * submitOrderParam.setPassType(dto.getBetType());
		 * submitOrderParam.setPlayType("0" + dto.getPlayType());
		 * submitOrderParam.setBetNum(dto.getBetNum());
		 * submitOrderParam.setCathectic(dto.getTimes()); if
		 * (lotteryPlayClassifyId != 8 && lotteryClassifyId == 1) { if
		 * (ticketDetails.size() > 1) { Optional<TicketDetail> max =
		 * ticketDetails.stream().max((detail1, detail2) ->
		 * detail1.getMatchTime().compareTo(detail2.getMatchTime()));
		 * submitOrderParam.setMatchTime(max.get().getMatchTime()); } else {
		 * submitOrderParam.setMatchTime(ticketDetails.get(0).getMatchTime()); }
		 * } submitOrderParam.setForecastMoney(dto.getForecastMoney());
		 * 
		 * submitOrderParam.setIssue(dto.getIssue());
		 * submitOrderParam.setTicketDetails(ticketDetails);
		 * BaseResult<OrderDTO> createOrder =
		 * orderService.createOrder(submitOrderParam); if (createOrder.getCode()
		 * != 0) { logger.info(loggerId + "订单创建失败！"); return
		 * ResultGenerator.genFailResult("支付失败！"); } String orderId =
		 * createOrder.getData().getOrderId().toString(); String orderSn =
		 * createOrder.getData().getOrderSn();
		 * 
		 * if (hasSurplus) { // 用户余额扣除 SurplusPayParam surplusPayParam = new
		 * SurplusPayParam(); surplusPayParam.setOrderSn(orderSn);
		 * surplusPayParam.setSurplus(surplus);
		 * surplusPayParam.setBonusMoney(bonusAmount); int payType1 = 2; if
		 * (hasThird) { payType1 = 3;
		 * 
		 * } surplusPayParam.setPayType(payType1);
		 * surplusPayParam.setMoneyPaid(surplus);
		 * surplusPayParam.setThirdPartName("");
		 * surplusPayParam.setThirdPartPaid(BigDecimal.ZERO); if (isSurplus) {
		 * BaseResult<SurplusPaymentCallbackDTO> changeUserAccountByPay =
		 * userAccountService.changeUserAccountByPay(surplusPayParam);
		 * logger.info("订单扣减用户余额orderSn={},返回信息code={}", orderSn,
		 * changeUserAccountByPay == null ? "" :
		 * changeUserAccountByPay.getCode()); if (changeUserAccountByPay == null
		 * || changeUserAccountByPay.getCode() != 0) {
		 * UpdateOrderStatusByAnotherStatusParam updateParams = new
		 * UpdateOrderStatusByAnotherStatusParam(); List<String> orderSnlist =
		 * new ArrayList<String>(); orderSnlist.add(orderSn);
		 * updateParams.setOrderSnlist(orderSnlist);
		 * updateParams.setOrderStatusAfter("8");
		 * updateParams.setOrderStatusBefore("0"); BaseResult<Integer>
		 * updateOrder = orderService.updateOrderStatusAnother(updateParams);
		 * 
		 * logger.info(loggerId + "用户余额扣减失败！orderSn={},订单由0更新为8响应code={}",
		 * orderSn, updateOrder == null ? "" : updateOrder.getCode()); return
		 * ResultGenerator.genFailResult("支付失败！"); } // 更新余额支付信息到订单 BigDecimal
		 * userSurplus = changeUserAccountByPay.getData().getUserSurplus();
		 * BigDecimal userSurplusLimit =
		 * changeUserAccountByPay.getData().getUserSurplusLimit();
		 * UpdateOrderInfoParam updateOrderInfoParam = new
		 * UpdateOrderInfoParam(); updateOrderInfoParam.setOrderSn(orderSn);
		 * updateOrderInfoParam.setUserSurplus(userSurplus);
		 * updateOrderInfoParam.setUserSurplusLimit(userSurplusLimit);
		 * BaseResult<String> updateOrderInfo =
		 * orderService.updateOrderInfo(updateOrderInfoParam); if
		 * (updateOrderInfo.getCode() != 0) { logger.info(loggerId +
		 * "订单回写用户余额扣减详情失败！"); BaseResult<SurplusPaymentCallbackDTO>
		 * rollbackUserAccountChangeByPay =
		 * userAccountService.rollbackUserAccountChangeByPay(surplusPayParam);
		 * logger.info(loggerId + " orderSn=" + orderSn + " , Surplus=" +
		 * surplus.doubleValue() + " 在回滚用户余额结束！ 订单回调返回结果：status=" +
		 * rollbackUserAccountChangeByPay.getCode() + " , message=" +
		 * rollbackUserAccountChangeByPay.getMsg()); if
		 * (rollbackUserAccountChangeByPay.getCode() != 0) {
		 * logger.info(loggerId + " orderSn=" + orderSn + " , Surplus=" +
		 * surplus.doubleValue() + " 在回滚用户余额时出错！"); } return
		 * ResultGenerator.genFailResult("支付失败！"); } } if (!hasThird) { //
		 * 回调order,更新支付状态,余额支付成功 UpdateOrderPayStatusParam param1 = new
		 * UpdateOrderPayStatusParam(); param1.setPayStatus(1); int currentTime
		 * = DateUtil.getCurrentTimeLong(); param1.setPayTime(currentTime);
		 * param1.setOrderSn(orderSn); param1.setPayCode("");
		 * param1.setPayName(""); param1.setPaySn(""); BaseResult<Integer>
		 * baseResult = orderService.updateOrderPayStatus(param1);
		 * logger.info(loggerId + " 订单成功状态更新回调返回结果：status=" +
		 * baseResult.getCode() + " , message=" + baseResult.getMsg() + "data="
		 * + baseResult.getData()); if (baseResult.getCode() != 0 && isSurplus
		 * && !Integer.valueOf(1).equals(baseResult.getData())) {
		 * BaseResult<SurplusPaymentCallbackDTO> rollbackUserAccountChangeByPay
		 * = userAccountService.rollbackUserAccountChangeByPay(surplusPayParam);
		 * logger.info(loggerId + " orderSn=" + orderSn + " , Surplus=" +
		 * surplus.doubleValue() + " 在订单成功状态更新回滚用户余额结束！ 订单回调返回结果：status=" +
		 * rollbackUserAccountChangeByPay.getCode() + " , message=" +
		 * rollbackUserAccountChangeByPay.getMsg()); if
		 * (rollbackUserAccountChangeByPay.getCode() != 0) {
		 * logger.info(loggerId + " orderSn=" + orderSn + " , Surplus=" +
		 * surplus.doubleValue() + " 在订单成功状态更新回滚用户余额时出错！"); } return
		 * ResultGenerator.genFailResult("支付失败！"); } logger.info(loggerId +
		 * "订单没有需要第三方支付金额，完全余额支付成功！"); PayReturnDTO payReturnDTO = new
		 * PayReturnDTO(); payReturnDTO.setOrderId(orderId); return
		 * ResultGenerator.genSuccessResult("支付成功！", payReturnDTO); } } //
		 * payCode处理 String payCode = paymentDto.getPayCode(); if
		 * ("app_weixin".equals(payCode)) { boolean isWechat =
		 * (param.getInnerWechat() == 1); Boolean openJianLian =
		 * paymentService.getJianLianIsOpen(); if (openJianLian) { isWechat =
		 * Boolean.TRUE; param.setInnerWechat(1); } if (isWechat) { payCode =
		 * "app_weixin" + "_h5"; } } int uid = SessionUtil.getUserId(); String
		 * payIp = this.getIpAddr(request); PayLog payLog = super.newPayLog(uid,
		 * orderSn, thirdPartyPaid, 0, payCode, paymentDto.getPayName(), payIp);
		 * PayLog savePayLog = payLogService.savePayLog(payLog); if (null ==
		 * savePayLog) { logger.info(loggerId + " payLog对象保存失败！"); return
		 * ResultGenerator.genFailResult("请求失败！", null); } else {
		 * logger.info("paylog save succ:" + " payLogId:" + payLog.getPayIp() +
		 * " paycode:" + payLog.getPayCode() + " payname:" +
		 * payLog.getPayName()); } // url下发后，服务器开始主动轮序订单状态 //
		 * PayManager.getInstance().addReqQueue(orderSn,savePayLog.getPayOrderSn
		 * (),paymentDto.getPayCode()); BaseResult payBaseResult = null; if
		 * ("app_zfb".equals(payCode)) { logger.info("支付宝支付url开始生成...isWechat:"
		 * + (param.getInnerWechat() == 1) + " payOrderSn:" +
		 * savePayLog.getPayOrderSn()); payBaseResult = getWechatPayUrl(true,
		 * param.getInnerWechat() == 1, param.getIsH5(), 0, savePayLog, payIp,
		 * orderId, ""); logger.info("支付宝支付url生成成功 code" +
		 * payBaseResult.getCode() + " data:" + payBaseResult.getData()); } else
		 * if ("app_weixin".equals(payCode) || "app_weixin_h5".equals(payCode))
		 * { logger.info("生成微信支付url:" + "inWechat:" + (param.getInnerWechat() ==
		 * 1) + " payCode:" + savePayLog.getPayCode()); payBaseResult =
		 * getWechatPayUrl(false, param.getInnerWechat() == 1, param.getIsH5(),
		 * 0, savePayLog, payIp, orderId, ""); if (payBaseResult != null &&
		 * payBaseResult.getData() != null) { String str =
		 * payBaseResult.getData() + ""; logger.info("生成支付url成功:" + str); } }
		 * else if ("app_rongbao".equals(paymentDto.getPayCode())) { // 生成支付链接信息
		 * String payOrder = savePayLog.getPayOrderSn(); ReqRongEntity reqEntity
		 * = new ReqRongEntity(); reqEntity.setOrderId(payOrder);
		 * reqEntity.setUserId(savePayLog.getUserId().toString());
		 * reqEntity.setTotal(savePayLog.getOrderAmount().doubleValue());
		 * reqEntity.setPName("彩小秘"); reqEntity.setPDesc("彩小秘足彩支付");
		 * reqEntity.setTransTime(savePayLog.getAddTime() + ""); String data =
		 * JSON.toJSONString(reqEntity); try { data = URLEncoder.encode(data,
		 * "UTF-8"); String url = rongCfg.getURL_PAY() + "?data=" + data;
		 * PayReturnDTO rEntity = new PayReturnDTO(); rEntity.setPayUrl(url);
		 * rEntity.setPayLogId(savePayLog.getLogId() + "");
		 * rEntity.setOrderId(orderId); payBaseResult =
		 * ResultGenerator.genSuccessResult("succ", rEntity); } catch
		 * (UnsupportedEncodingException e) { e.printStackTrace(); } } else if
		 * ("app_xianfeng".equals(paymentDto.getPayCode())) { PayReturnDTO
		 * rEntity = new PayReturnDTO();
		 * rEntity.setPayUrl(xianFengUtil.getPayH5Url(savePayLog.getLogId()));
		 * rEntity.setPayLogId(savePayLog.getLogId() + "");
		 * rEntity.setOrderId(orderId); payBaseResult =
		 * ResultGenerator.genSuccessResult("succ", rEntity); } else if
		 * ("app_yifutong".equals(paymentDto.getPayCode())) {
		 * logger.info("生成易富通支付宝支付url:" + " payCode:" +
		 * savePayLog.getPayCode()); payBaseResult =
		 * paymentService.getYFTPayUrl(savePayLog, orderId, ""); if
		 * (payBaseResult != null && payBaseResult.getData() != null) { String
		 * str = payBaseResult.getData() + ""; logger.info("生成易富通支付url成功:" +
		 * str); } else { logger.info("生成易富通支付url失败"); } } else if
		 * (paymentDto.getPayCode().startsWith("app_tianxia_scan")) { String[]
		 * merchentArr = paymentDto.getPayCode().split("_");
		 * logger.info("生成天下支付银联二维码url:" + " payCode:" +
		 * savePayLog.getPayCode()); payBaseResult =
		 * paymentService.getTXScanPayUrl(savePayLog, orderId, payIp,
		 * merchentArr[merchentArr.length - 1]); if (payBaseResult != null &&
		 * payBaseResult.getData() != null) { String str =
		 * payBaseResult.getData() + ""; logger.info("生成天下支付银联二维码url成功:" + str);
		 * } else { logger.info("生成天下支付银联二维码url失败"); } } else if
		 * ("app_kuaijie_pay_qqqianbao".equalsIgnoreCase(payCode)) {
		 * logger.info("生成快接支付qq钱包支付 payCode:" + savePayLog.getPayCode());
		 * payBaseResult = paymentService.getKuaijiePayQqQianBaoUrl(savePayLog,
		 * orderId,""); if (payBaseResult != null && payBaseResult.getData() !=
		 * null) { String str = payBaseResult.getData() + "";
		 * logger.info("生成快接支付qq钱包支付payOrderSn={},url成功url={}:", orderSn, str);
		 * } else { logger.info("生成快接支付qq钱包支付payOrderSn={},url失败", orderSn); } }
		 * else if ("app_kuaijie_pay_jd".equalsIgnoreCase(payCode)) {
		 * logger.info("生成快接支付url:payCode:" + savePayLog.getPayCode());
		 * payBaseResult = paymentService.getKuaijiePayJingDongUrl(savePayLog,
		 * orderId,""); if (payBaseResult != null && payBaseResult.getData() !=
		 * null) { String str = payBaseResult.getData() + "";
		 * logger.info("生成快接支付payOrderSn={},url成功 url={}:", orderSn, str); }
		 * else { logger.info("生成快接支付payOrderSn={},url失败", orderSn); } }else if
		 * ("app_ubey".equals(paymentDto.getPayCode())) {
		 * logger.info("ubey支付url:" + " payCode:" + savePayLog.getPayCode());
		 * payBaseResult = ubeyPayService.getUBeyBankUrl(savePayLog, orderId);
		 * if (payBaseResult != null && payBaseResult.getData() != null) {
		 * String str = payBaseResult.getData() + "";
		 * logger.info("获取Ubey银行列表成功:" + str); } else {
		 * logger.info("获取Ubey银行列表失败"); } } logger.info(loggerId +
		 * " result: code=" + payBaseResult.getCode() + " , msg=" +
		 * payBaseResult.getMsg()); return payBaseResult;
		 */
	}

	/***
	 * 根据savePayLog生成微信支付链接
	 * 
	 * @param savePayLog
	 * @param payIp
	 * @param orderId
	 * @param payType
	 *            0->支付 1->充值
	 * @return
	 */
	private BaseResult<?> getWechatPayUrl(Boolean isZfb, boolean isInnerWeChat,
			String isH5, int payType, PayLog savePayLog, String payIp,
			String orderId, String lotteryClassifyId) {
		String payFinishRedirectURL = "";// paymentService.payFinishRedirectUrlPlusParams(cfgPay.getURL_REDIRECT_APP());
		BaseResult<?> payBaseResult = null;
		BigDecimal amtDouble = savePayLog.getOrderAmount();
		BigDecimal bigD = amtDouble.multiply(BigDecimal.valueOf(100))
				.setScale(0, RoundingMode.HALF_EVEN);
		String payOrderSn = savePayLog.getPayOrderSn();
		String payLogId = savePayLog.getLogId() + "";
		RspYinHeEntity rYinHeEntity = null;
		if (isZfb) {
			rYinHeEntity = payUtil.getWechatPayUrl(payFinishRedirectURL, true,
					true, payIp, bigD.toString(), payOrderSn);
		} else if (isInnerWeChat) {
			rYinHeEntity = payUtil.getWechatPayUrl(payFinishRedirectURL, false,
					true, payIp, bigD.toString(), payOrderSn);
		} else {
			rYinHeEntity = payUtil.getWechatPayUrl(payFinishRedirectURL, false,
					false, payIp, bigD.toString(), payOrderSn);
		}
		if (rYinHeEntity != null) {
			if (rYinHeEntity.isSucc()
					&& !TextUtils.isEmpty(rYinHeEntity.qrCode)) {
				PayReturnDTO rEntity = new PayReturnDTO();
				String encodeUrl = null;
				String redirectUri = null;
				String url = null;
				if (!isZfb && !isInnerWeChat) {
					try {
						String qrCode = rYinHeEntity.qrCode;
						encodeUrl = URLEncoder.encode(qrCode, "UTF-8");
						if ("1".equals(isH5)) {
							redirectUri = URLEncoder
									.encode(cfgPay.getURL_REDIRECT_H5()
											+ "?payLogId=" + payLogId, "UTF-8");
						} else {
							redirectUri = URLEncoder
									.encode(cfgPay.getURL_REDIRECT_APP()
											+ "?payLogId=" + payLogId, "UTF-8");
						}
					} catch (UnsupportedEncodingException e) {
						logger.error("获取微信支付地址异常", e);
					}
					if (!TextUtils.isEmpty(encodeUrl)) {
						if ("1".equals(isH5)) {
							url = cfgPay.getURL_PAY_WECHAT_H5() + "?data="
									+ encodeUrl + "&redirect_uri="
									+ redirectUri;
						} else {
							url = cfgPay.getURL_PAY_WECHAT_APP() + "?data="
									+ encodeUrl + "&redirect_uri="
									+ redirectUri;
						}
					} else {
						logger.info("encodeUrl失败~");
					}
				} else {
					url = rYinHeEntity.qrCode;
					Boolean openJianLian = paymentService.getJianLianIsOpen();
					if (openJianLian) {
						String payName = "微信;";
						if (isZfb) {
							payName = "支付宝;";
						}
						String amount = payName + "￥" + amtDouble
								.setScale(2, RoundingMode.HALF_EVEN).toString();
						logger.info("间联开关打开,原url={}，生成二维码地址开始,amtDoubleStr={}",
								url, amount);
						try {
							ByteArrayOutputStream out = new ByteArrayOutputStream();
							BufferedImage bufferImage = QrUtil.genBarcode(url,
									520, 520, amount);
							ImageIO.write(bufferImage, "png", out);
							byte[] imageB = out.toByteArray();
							// sun.misc.BASE64Encoder encoder = new
							// sun.misc.BASE64Encoder();
							String qrBase64 = "data:image/png;base64,"
									+ Base64.encodeBase64String(imageB);
							DlPayQrBase64 saveBean = new DlPayQrBase64();
							saveBean.setPayordersn(payOrderSn);
							saveBean.setBase64Content(qrBase64);
							Integer insertRow = dlPayQrBase64Mapper
									.saveDlPayQrBase64(saveBean);
							Integer base64Id = saveBean.getId();
							if (isZfb) {
								url = appZFBH5QrUrl.replace("{qrBase64}",
										"" + base64Id);
							} else {
								url = appH5QrUrl.replace("{qrBase64}",
										"" + base64Id);
							}
							// url = URLEncoder.encode(url,"UTF-8");
							// logger.info("url={},base64Id={},encode Url
							// base64Url={}",url,base64Id,qrBase64);
							logger.info(
									"payOrderSn={},支付方式={},url={},base64Id={}",
									payOrderSn, isZfb ? "支付宝" : "微信", url,
									base64Id);
						} catch (Exception e) {
							logger.error("微信转二维码异常", e);
						}
					}
				}
				if (!TextUtils.isEmpty(url)) {
					rEntity.setPayUrl(url);
					rEntity.setPayLogId(savePayLog.getLogId() + "");
					rEntity.setOrderId(orderId);
					rEntity.setLotteryClassifyId(lotteryClassifyId);
					logger.info("client jump url:" + url + " payLogId:"
							+ savePayLog.getLogId() + " orderId:" + orderId
							+ " inWechat:" + isInnerWeChat);
					payBaseResult = ResultGenerator.genSuccessResult("succ",
							rEntity);
				} else {
					payBaseResult = ResultGenerator
							.genFailResult("url decode失败", null);
				}
			} else {
				payBaseResult = ResultGenerator.genResult(
						PayEnums.PAY_YINHE_INNER_ERROR.getcode(),
						PayEnums.PAY_YINHE_INNER_ERROR.getMsg() + "["
								+ rYinHeEntity.returnMsg + "]");
			}
		} else {
			payBaseResult = ResultGenerator.genFailResult("银河支付返回数据有误");
		}
		return payBaseResult;
	}
	
	@ApiOperation(value = "跳转支付页面", notes = "payCode：支付编码")
	@PostMapping("/forPayPage")
	@ResponseBody
	public BaseResult<Object> forPayPage(@RequestBody RechargeParam param,HttpServletRequest request) {
		Integer userid = SessionUtil.getUserId();
		if(userid==null) {
			return ResultGenerator.genFailResult("获取用户信息失败。");
		}
		com.dl.member.param.UserIdParam params = new com.dl.member.param.UserIdParam();
		params.setUserId(userid);
		BaseResult<UserDTO> resultUser = userService.queryUserInfo(params);
		if(resultUser==null || resultUser.getData()==null) {
			return ResultGenerator.genFailResult("获取用户信息失败。");
		}
		String loggerId = "forPayPage" + System.currentTimeMillis();
		logger.info(loggerId + " int /payment/forPayPage, userId="
				+ userid + " ,payCode=" + param.getPayCode()
				+ " , totalAmount=" + param.getTotalAmount());
		double totalAmount = param.getTotalAmount();
		BaseResult<Object> payBaseResult = null;
		if (totalAmount < 1) {
			return ResultGenerator.genFailResult("请选择固额充值。");
		}
		
		//获取支付链接
		BaseResult<PaymentDTO> resultPayment = paymentService.queryByCode("app_smk");
		if(resultPayment==null || resultPayment.getData()==null) {
			return ResultGenerator.genFailResult("获取第三方支付方式失败。");
		}
		
		PaymentDTO paymentDTO = resultPayment.getData();
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("payUrl",paymentDTO.getPayUrl()+"?merCustId="+userid+"&amount="+totalAmount);//h5链接
		payBaseResult = ResultGenerator.genSuccessResult("succ", paramMap);
		return payBaseResult;
	}
	
	@ApiOperation(value = "根据用户id获取页面展示信息", notes = "payCode：支付编码")
	@PostMapping("/forPageInfo")
	@ResponseBody
	public BaseResult<Object> forPageInfo(@RequestBody RechargeParam param,HttpServletRequest request) {
		Integer userid = param.getMerCustId();
		if(userid==null) {
			userid = SessionUtil.getUserId();
			if(userid==null) {
				return ResultGenerator.genFailResult("获取用户信息失败。");
			}
		}
		com.dl.member.param.UserIdParam params = new com.dl.member.param.UserIdParam();
		params.setUserId(userid);
		BaseResult<UserDTO> resultUser = userService.queryUserInfo(params);
		if(resultUser==null || resultUser.getData()==null) {
			return ResultGenerator.genFailResult("获取用户信息失败。");
		}
		BaseResult<Object> payBaseResult = null;
		//获取当前用户身份证及默认银行卡信息
		UserBankQueryParam ubqp = new UserBankQueryParam();
		ubqp.setUserId(userid);
		ubqp.setBankCardCode(userid+"");//此处赋值是为了通过model校验，实际参数未用到
		BaseResult<UserBankDTO>  resultBank = userBankService.queryUserBankByUserId(ubqp);
		if(resultBank==null || resultBank.getData()==null) {
			return ResultGenerator.genFailResult("获取银行卡信息失败,请核实是否已绑定银行卡。");
		}
		UserBankDTO userBank = resultBank.getData();
		String cardNoHide=userBank.getCardNo().substring(0,4)+"*********"+userBank.getCardNo().substring(userBank.getCardNo().length()-4);
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("cardNoHide", cardNoHide);
		paramMap.put("phone", resultUser.getData().getRealmobile());
		payBaseResult = ResultGenerator.genSuccessResult("succ", paramMap);
		return payBaseResult;
	}
	
	@ApiOperation(value = "获取惠民支付获取相关验证码", notes = "payCode：支付编码")
	@PostMapping("/forRecharge")
	@ResponseBody
	public BaseResult<Object> forRecharge(@RequestBody RechargeParam param,HttpServletRequest request) {
		Integer userid = param.getMerCustId();
		if(userid==null) {
			userid = SessionUtil.getUserId();
			if(userid==null) {
				return ResultGenerator.genFailResult("获取用户信息失败。");
			}
		}
		String loggerId = "forRecharge" + System.currentTimeMillis();
		logger.info(loggerId + " int /payment/forRecharge, userId="
				+ userid + " ,payCode=" + param.getPayCode()
				+ " , totalAmount=" + param.getTotalAmount());
		double totalAmount = param.getTotalAmount();
		BaseResult<Object> payBaseResult = null;
		if (totalAmount < 1) {
			return ResultGenerator.genFailResult("请选择固额充值 ");
		}
		
		//获取当前用户身份证及默认银行卡信息
		boolean isSign = false;
		//获取当前用户身份证及默认银行卡信息
		UserBankQueryParam ubqp = new UserBankQueryParam();
		ubqp.setUserId(userid);
		ubqp.setBankCardCode(userid+"");//此处赋值是为了通过model校验，实际参数未用到
		BaseResult<UserBankDTO>  resultBank = userBankService.queryUserBankByUserId(ubqp);
		if(resultBank==null || resultBank.getData()==null) {
			return ResultGenerator.genFailResult("银行卡信息获取失败。");
		}
		UserBankDTO userbank = resultBank.getData();
		if(userbank.getIsSign()==1) {
			isSign = true;
		}
		Map<String, String> resultMap = null;
		String rechargeSn = SNGenerator.nextSN(SNBusinessCodeEnum.RECHARGE_SN.getCode());
		Map<String, String> paramMap = new HashMap<String, String>();
		Map<String, String> resultHFMap = new HashMap<String, String>();
		resultHFMap.put("orderNo", rechargeSn);
		try {
			if(isSign) {
				paramMap.put("merCustId", userid+"");//用户ID
				paramMap.put("orderNo", rechargeSn);
				paramMap.put("amount", totalAmount+"");
				resultMap = smkPayService.bqpPay(paramMap);//银行卡信息已经签约，直接支付
				resultHFMap.put("phoneToken", resultMap.get("phoneToken"));
			}else {
				UserIdRealParam ureal = new UserIdRealParam();
				ureal.setUserId(userid);
				BaseResult<UserRealDTO> resultUserReal = userService.queryUserRealByUserId(ureal);
				if(resultUserReal==null || resultUserReal.getData()==null) {
					return ResultGenerator.genFailResult("用户实名认证信息获取失败。");
				}
				UserRealDTO userDto = resultUserReal.getData();
				paramMap.put("merCustId", SessionUtil.getUserId()+"");//用户ID
				paramMap.put("name", userbank.getRealName());
				paramMap.put("certType", "0");//身份证
				paramMap.put("certNo", userDto.getIdCode());
				paramMap.put("phone", param.getPhone());
				paramMap.put("cardType", "D");//借记卡
				paramMap.put("cardNo", userbank.getCardNo());
				paramMap.put("orderNo", rechargeSn);
				paramMap.put("amount", totalAmount+"");
				resultMap = smkPayService.bqpSignAndPay(paramMap);//银行卡信息未签约并支付
				resultHFMap.put("token", resultMap.get("token"));
				resultHFMap.put("phoneToken", resultMap.get("phoneToken"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(resultMap==null) {
				return ResultGenerator.genFailResult("充值失败 ");
			}
			if("55".equals(resultMap.get("status"))){
				return ResultGenerator.genFailResult(resultMap.get("respDesc"));
			}
		}
		payBaseResult = ResultGenerator.genSuccessResult("succ", resultHFMap);
		return payBaseResult;
	}
	
	@ApiOperation(value = "app充值调用", notes = "payCode：支付编码，app端微信支付为app_weixin")
	@PostMapping("/recharge")
	@ResponseBody
	public BaseResult<Object> rechargeForApp(@RequestBody RechargeParam param,
			HttpServletRequest request) {
		// 20181203 加入提示
		// return ResultGenerator.genResult(PayEnums.PAY_STOP_SERVICE.getcode(),
		// PayEnums.PAY_STOP_SERVICE.getMsg());
		String loggerId = "rechargeForApp_" + System.currentTimeMillis();
		logger.info(loggerId + " int /payment/recharge, userId="
				+ SessionUtil.getUserId() + " ,payCode=" + param.getPayCode()
				+ " , totalAmount=" + param.getTotalAmount());
		UserDeviceInfo userDeviceInfo = SessionUtil.getUserDevice();
		String appCodeName = userDeviceInfo.getAppCodeName();
		logger.info("当前平台是====appCodeName=" + appCodeName);
		if (!"11".equals(appCodeName)) {
			if (paymentService.isShutDownPay()) {
				return ResultGenerator.genResult(PayEnums.PAY_STOP.getcode(),
						PayEnums.PAY_STOP.getMsg());
			}
		}
		double totalAmount = param.getTotalAmount();
		// if (totalAmount <= 0) {
		// logger.info(loggerId + "充值金额有误！totalAmount=" + totalAmount);
		// return
		// ResultGenerator.genResult(PayEnums.RECHARGE_AMT_ERROR.getcode(),
		// PayEnums.RECHARGE_AMT_ERROR.getMsg());
		// }
		// 当前支付方式限额10万/笔
		// if (totalAmount > 100000) {
		// logger.info(loggerId + "每笔限额10w");
		// return ResultGenerator.genResult(PayEnums.PAY_RECHARGE_MAX.getcode(),
		// PayEnums.PAY_RECHARGE_MAX.getMsg());
		// }
		// 支付方式
		String payCode = param.getPayCode();
		if (StringUtils.isBlank(payCode)) {
			logger.info(loggerId + "订单第三支付没有提供paycode！");
			return ResultGenerator.genResult(
					PayEnums.RECHARGE_PAY_STYLE_EMPTY.getcode(),
					PayEnums.RECHARGE_PAY_STYLE_EMPTY.getMsg());
		}

		// 各支付方式最小金额检验
		if ("app_lidpay".equals(param.getPayCode())) {
			if (totalAmount < 1) {
				return ResultGenerator.genFailResult("单笔充值金额不能低于1元 ");
			}
			if (totalAmount > 10000) {
				return ResultGenerator.genFailResult("单笔充值金额不能超过10000元 ");
			}
		} else if ("app_apay".equals(param.getPayCode())) {
			if (totalAmount < 1) {
				return ResultGenerator.genFailResult("单笔充值金额不能低于1元 ");
			}
			if (totalAmount > 10000) {
				return ResultGenerator.genFailResult("单笔充值金额不能超过10000元 ");
			}
		} else if ("app_awx".equals(param.getPayCode())) {
			if (totalAmount < 500) {
				return ResultGenerator.genFailResult("单笔充值金额不能低于500元 ");
			}
			if (totalAmount > 10000) {
				return ResultGenerator.genFailResult("单笔充值金额不能超过10000元 ");
			}
		} else if ("app_awxxe".equals(param.getPayCode())) {
			if (totalAmount < 10) {
				return ResultGenerator.genFailResult("单笔充值金额不能低于10元 ");
			}
			if (totalAmount > 300) {
				return ResultGenerator.genFailResult("单笔充值金额不能超过300元 ");
			}
		} else if ("app_rkquick".equals(param.getPayCode())) {
			if (totalAmount < 20) {
				return ResultGenerator.genFailResult("单笔充值金额不能低于20元 ");
			}
			if (totalAmount > 5000) {
				return ResultGenerator.genFailResult("单笔充值金额不能超过5000元 ");
			}
		} else if ("app_rkwap".equals(param.getPayCode())) {
			if (totalAmount < 80) {
				return ResultGenerator.genFailResult("请选择固额充值 ");
			}
		} else if ("app_jhpay".equals(param.getPayCode())) {
			if (totalAmount < 1) {
				return ResultGenerator.genFailResult("请选择固额充值 ");
			}
		} else if ("app_yunpay".equals(param.getPayCode())) {
			if (totalAmount < 1) {
				return ResultGenerator.genFailResult("请选择固额充值 ");
			}
		} else if ("app_smk".equals(param.getPayCode())) {
			if (totalAmount < 1) {
				return ResultGenerator.genFailResult("请选择固额充值 ");
			}
		}
		

		BaseResult<PaymentDTO> paymentResult = paymentService
				.queryByCode(payCode);
		if (paymentResult.getCode() != 0) {
			logger.info(loggerId + "订单第三方支付提供paycode有误！");
			return ResultGenerator.genResult(
					PayEnums.RECHARGE_PAY_STYLE_EMPTY.getcode(),
					PayEnums.RECHARGE_PAY_STYLE_EMPTY.getMsg());
		}
		// 生成充值记录payLog
		String payName = paymentResult.getData().getPayName();
		// 生成充值单 金额由充值金额和赠送金额组成
		int givemoney = 0;
		if ("app_rkwap".equals(payCode) || "app_jhpay".equals(payCode)) {// Q多多支付宝快捷支付附加固额充值赠送
			PaymentDTO paymentdto = paymentResult.getData();
			if (paymentdto != null) {
				int isreadonly = paymentdto.getIsReadonly();
				if (isreadonly == 1) {// 固额充值赠送
					List<Map<String, String>> maps = paymentdto.getReadMoney();
					for (Map<String, String> map : maps) {
						String readmoney = map.get("readmoney");
						if (StringUtil.isEmpty(readmoney)) {
							readmoney = "0";// 103 298
						}
						if (param.getTotalAmount() >= Integer
								.parseInt(readmoney)
								&& param.getTotalAmount() <= (Integer
										.parseInt(readmoney) + 5)) {
							givemoney = Integer.parseInt(!StringUtils
									.isNotEmpty(map.get("givemoney"))
											? "0"
											: map.get("givemoney"));
							break;
						}
					}
				}
			}
		}

		if ("app_yunpay".equals(payCode)) {// 云闪付大额支付宝快捷支付附加固额充值赠送
			PaymentDTO paymentdto = paymentResult.getData();
			if (paymentdto != null) {
				int isreadonly = paymentdto.getIsReadonly();
				if (isreadonly == 1) {// 固额充值赠送
					List<Map<String, String>> maps = paymentdto.getReadMoney();
					for (Map<String, String> map : maps) {
						String readmoney = map.get("readmoney");
						if (StringUtil.isEmpty(readmoney)) {
							readmoney = "0";
						}
						if (param.getTotalAmount() == Integer
								.parseInt(readmoney)) {
							givemoney = Integer.parseInt(!StringUtils
									.isNotEmpty(map.get("givemoney"))
											? "0"
											: map.get("givemoney"));
							break;
						}
					}
				}
			}
		}

		logger.info(loggerId + "赠送金额为" + givemoney);
		String rechargeSn = "";
		if ("app_smk".equals(payCode)) {
			rechargeSn = userRechargeService.saveReCharege(
					BigDecimal.valueOf(totalAmount + givemoney), payCode, payName,param.getOrderSn());
		}else {
			rechargeSn = userRechargeService.saveReCharege(
					BigDecimal.valueOf(totalAmount + givemoney), payCode, payName);
		}
		
		if (StringUtils.isEmpty(rechargeSn)) {
			logger.info(loggerId + "生成充值单失败");
			return ResultGenerator.genFailResult("充值失败！", null);
		}
		String orderSn = rechargeSn;
		String payIp = this.getIpAddr(request);
		// payCode处理
		if ("app_weixin".equals(payCode)) {
			boolean isWechat = (param.getInnerWechat() == 1);
			Boolean openJianLian = paymentService.getJianLianIsOpen();
			if (openJianLian) {
				isWechat = Boolean.TRUE;
				param.setInnerWechat(1);
			}
			if (isWechat) {
				payCode = "app_weixin" + "_h5";
			}
		}
		Integer userId = SessionUtil.getUserId();
		PayLog payLog = super.newPayLog(userId, orderSn,
				BigDecimal.valueOf(totalAmount), 1, payCode, payName, payIp,
				givemoney + "");
		PayLog savePayLog = payLogService.savePayLog(payLog);
		if (null == savePayLog) {
			logger.info(loggerId + " payLog对象保存失败！");
			return ResultGenerator.genFailResult("请求失败！", null);
		} else {
			logger.info("save paylog succ:" + " id:" + payLog.getLogId()
					+ " paycode:" + payCode + " payOrderSn:"
					+ payLog.getPayOrderSn());
		}

		// 第三方支付调用
		UnifiedOrderParam unifiedOrderParam = new UnifiedOrderParam();
		unifiedOrderParam.setBody("余额充值");
		unifiedOrderParam.setSubject("余额充值");
		unifiedOrderParam.setTotalAmount(totalAmount);
		unifiedOrderParam.setIp(payIp);
		unifiedOrderParam.setOrderNo(savePayLog.getLogId());
		// url下发后，服务器开始主动轮序订单状态
		// PayManager.getInstance().addReqQueue(orderSn,savePayLog.getPayOrderSn(),payCode);
		BaseResult payBaseResult = null;
		if ("app_jhpay".equals(param.getPayCode())) {
			Map<String, Object> jhmap = new HashMap<>();
			jhmap.put("orderId", orderSn);
			jhmap.put("orderSn", orderSn);
			jhmap.put("payLogId", savePayLog.getLogId());
			payBaseResult = ResultGenerator.genSuccessResult("succ", jhmap);
			return payBaseResult;
		}
		
		if ("app_lidpay".equals(payCode)) {
			logger.info("华移支付url:" + " payCode:" + savePayLog.getPayCode());
			payBaseResult = lidPayService.getLidPayUrl(savePayLog, orderSn,
					orderSn, "充值");
			if (payBaseResult != null && payBaseResult.getData() != null) {
				String str = payBaseResult.getData() + "";
				logger.info("生成华移支付payOrderSn={},url成功 url={}:", orderSn, str);
			} else {
				logger.info("生成华移支付payOrderSn={},url失败", orderSn);
			}
		} else if ("app_apay".equals(payCode)) {
			logger.info("艾支付url:" + " payCode:" + savePayLog.getPayCode());
			// String channel_id = "9";//渠道编号 微信支付(扫码):6 微信支付H5:7 支付宝支付：9
			// int iswechat = param.getInnerWechat();
			// if(iswechat==1) {
			// channel_id = "6";
			// }
			payBaseResult = aPayService.getAPayUrl(savePayLog, orderSn, orderSn,
					userId, "9", "127.0.0.1", "充值");
			if (payBaseResult != null && payBaseResult.getData() != null) {
				String str = payBaseResult.getData() + "";
				logger.info("生成艾支付payOrderSn={},url成功 url={}:", orderSn, str);
			} else {
				logger.info("生成艾支付payOrderSn={},url失败", orderSn);
			}
		} else if ("app_awx".equals(payCode)) {
			logger.info("艾支付url:" + " payCode:" + savePayLog.getPayCode());
			// String channel_id = "9";//渠道编号 微信支付(扫码):6 微信支付H5:7 支付宝支付：9
			// int iswechat = param.getInnerWechat();
			// if(iswechat==1) {
			// channel_id = "6";
			// }
			payBaseResult = aPayService.getAPayUrl(savePayLog, orderSn, orderSn,
					userId, "6", "127.0.0.1", "充值");
			if (payBaseResult != null && payBaseResult.getData() != null) {
				String str = payBaseResult.getData() + "";
				logger.info("生成艾支付payOrderSn={},url成功 url={}:", orderSn, str);
			} else {
				logger.info("生成艾支付payOrderSn={},url失败", orderSn);
			}
		} else if ("app_awxxe".equals(payCode)) {
			logger.info("艾支付url:" + " payCode:" + savePayLog.getPayCode());
			// String channel_id = "9";//渠道编号 微信支付(扫码):6 微信支付H5:7 支付宝支付：9
			// int iswechat = param.getInnerWechat();
			// if(iswechat==1) {
			// channel_id = "6";
			// }
			payBaseResult = aPayService.getAPayUrl(savePayLog, orderSn, orderSn,
					userId, "6", "127.0.0.1", "充值");
			if (payBaseResult != null && payBaseResult.getData() != null) {
				String str = payBaseResult.getData() + "";
				logger.info("生成艾支付payOrderSn={},url成功 url={}:", orderSn, str);
			} else {
				logger.info("生成艾支付payOrderSn={},url失败", orderSn);
			}
		} else if ("app_rkquick".equals(payCode)) {
			logger.info(
					"Q多多网银快捷支付url:" + " payCode:" + savePayLog.getPayCode());
			payBaseResult = rkPayService.getRkPayQuickUrl(savePayLog, "NORMAL",
					orderSn, orderSn, "充值", "", "", "", "", "");
			if (payBaseResult != null && payBaseResult.getData() != null) {
				String str = payBaseResult.getData() + "";
				logger.info("生成Q多多网银快捷支付payOrderSn={},url成功 url={}:", orderSn,
						str);
			} else {
				logger.info("生成Q多多网银快捷支付payOrderSn={},url失败", orderSn);
			}
		} else if ("app_rkwap".equals(payCode)) {
			logger.info("Q多多支付宝支付url:" + " payCode:" + savePayLog.getPayCode());
			payBaseResult = rkPayService.getRkPayWapUrl(savePayLog, orderSn,
					orderSn, "充值");
			if (payBaseResult != null && payBaseResult.getData() != null) {
				String str = payBaseResult.getData() + "";
				logger.info("生成Q多多支付宝支付payOrderSn={},url成功 url={}:", orderSn,
						str);
			} else {
				logger.info("生成Q多多支付宝支付payOrderSn={},url失败", orderSn);
			}
		} else if ("app_yunpay".equals(param.getPayCode())) {
			logger.info("云闪付支付url:" + " payCode:" + savePayLog.getPayCode());

			payBaseResult = yunPayService.getYunPayUrl(savePayLog, orderSn,
					orderSn, "alipay", "充值");
			if (payBaseResult != null && payBaseResult.getData() != null) {
				String str = payBaseResult.getData() + "";
				logger.info("生成云闪付支付payOrderSn={},url成功 url={}:", orderSn, str);
			} else {
				logger.info("生成云闪付支付payOrderSn={},url失败", orderSn);
			}
		}else if ("app_smk".equals(param.getPayCode())) {
			//获取当前用户身份证及默认银行卡信息
			boolean isSign = false;
			//获取当前用户身份证及默认银行卡信息
			UserBankQueryParam ubqp = new UserBankQueryParam();
			Integer userid = param.getMerCustId();
			if(userid==null) {
				userid = userId;
				if(userid==null) {
					return ResultGenerator.genFailResult("用户信息获取失败。");
				}
			}
			ubqp.setUserId(userId);
			ubqp.setBankCardCode(userId+"");//此处赋值是为了通过model校验，实际参数未用到
			BaseResult<UserBankDTO>  resultBank = userBankService.queryUserBankByUserId(ubqp);
			if(resultBank==null || resultBank.getData()==null) {
				return ResultGenerator.genFailResult("银行卡信息获取失败。");
			}
			UserBankDTO userbank = resultBank.getData();
			if(userbank.getIsSign()==1) {
				isSign = true;
			}
			Map<String, String> paramMap = new HashMap<String, String>();
			Map<String, String> resultMap = new HashMap<String, String>();
			try {
				if(isSign) {
					paramMap.put("merCustId", userid+"");//用户ID
					paramMap.put("orderNo", param.getOrderSn());
					paramMap.put("amount", totalAmount+"");
					paramMap.put("phoneToken", param.getPhoneToken());
					resultMap = smkPayService.bqpPay(paramMap);//银行卡信息已经签约，直接支付
					
				}else {
					UserIdRealParam ureal = new UserIdRealParam();
					ureal.setUserId(userid);
					BaseResult<UserRealDTO> resultUserReal = userService.queryUserRealByUserId(ureal);
					if(resultUserReal==null || resultUserReal.getData()==null) {
						return ResultGenerator.genFailResult("用户实名认证信息获取失败。");
					}
					UserRealDTO userDto = resultUserReal.getData();
					paramMap.put("merCustId", SessionUtil.getUserId()+"");//用户ID
					paramMap.put("name", userbank.getRealName());
					paramMap.put("certType", "0");//身份证
					paramMap.put("certNo", userDto.getIdCode());
					paramMap.put("phone", param.getPhone());
					paramMap.put("cardType", "D");//借记卡
					paramMap.put("cardNo", userbank.getCardNo());
					paramMap.put("orderNo", param.getOrderSn());
					paramMap.put("amount", totalAmount+"");
					paramMap.put("token", param.getTonken());
					paramMap.put("phoneToken", param.getPhoneToken());
					resultMap = smkPayService.bqpSignAndPay(paramMap);//银行卡信息未签约并支付
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if(resultMap!=null) {
					Map<String,String> resultHf = new HashMap<String,String> ();
					if("55".equals(resultMap.get("status"))){
						payBaseResult = ResultGenerator.genFailResult(resultMap.get("respDesc"));
					}else {
						resultHf.put("status", resultMap.get("status"));
						this.smkPayNotify(param.getOrderSn(),resultMap.get("status"));//支付成功后回调操作 处理订单状态 账户余额
						payBaseResult = ResultGenerator.genSuccessResult("succ", resultHf);
					}
				}else {
					payBaseResult = ResultGenerator.genFailResult("惠民支付返回数据有误");
				}
			}
		}
		// 处理支付失败的情况
		if (null == payBaseResult || payBaseResult.getCode() != 0) {
			// 充值失败逻辑
			// 更改充值单状态
			UpdateUserRechargeParam updateUserParams = new UpdateUserRechargeParam();
			updateUserParams.setPaymentCode(payLog.getPayCode());
			updateUserParams.setPaymentId(payLog.getLogId() + "");
			updateUserParams.setPaymentName(payLog.getPayName());
			updateUserParams.setPayTime(DateUtil.getCurrentTimeLong());
			updateUserParams.setRechargeSn(rechargeSn);
			updateUserParams.setStatus("2");
			BaseResult<String> baseResult = userRechargeService
					.updateReCharege(updateUserParams);
			logger.info(
					loggerId + " 充值失败更改充值单返回信息：status=" + baseResult.getCode()
							+ " , message=" + baseResult.getMsg());
			if (baseResult.getCode() == 0) {
				// 更改流水信息
				try {
					PayLog updatePayLog = new PayLog();
					updatePayLog.setLogId(savePayLog.getLogId());
					updatePayLog.setIsPaid(0);
					updatePayLog.setPayMsg(baseResult.getMsg());
					payLogService.updatePayMsg(updatePayLog);
				} catch (Exception e) {
					logger.error(loggerId + "paylogid=" + savePayLog.getLogId()
							+ " , paymsg=" + baseResult.getMsg() + "保存失败记录时出错",
							e);
				}
			}
		}
		if (payBaseResult != null) {
			logger.info(loggerId + " result: code=" + payBaseResult.getCode()
					+ " , msg=" + payBaseResult.getMsg());
			return payBaseResult;
		} else {
			return ResultGenerator.genFailResult("参数异常");
		}
	}

	@ApiOperation(value = "app提现调用", notes = "")
	@PostMapping("/withdraw")
	@ResponseBody
	public BaseResult<Object> withdrawForApp(@RequestBody WithdrawParam param,
			HttpServletRequest request) {
		// 20181203 加入提示
		String loggerId = "withdrawForApp_" + System.currentTimeMillis();
		logger.info(loggerId + " int /payment/withdraw, userId="
				+ SessionUtil.getUserId() + ", totalAmount="
				+ param.getTotalAmount() + ",userBankId="
				+ param.getUserBankId());
		BaseResult<UserDTO> userInfoExceptPass = userService
				.userInfoExceptPass(new StrParam());
		if (userInfoExceptPass == null) {
			return ResultGenerator.genFailResult("对不起，用户信息有误！", null);
		}

		double totalAmount = 0;
		if (totalAmount <= 0) {
			logger.info(loggerId + "提现金额提供有误！");
			return ResultGenerator.genFailResult("对不起，请提供有效的提现金额！", null);
		}

		// 支付方式
		int userBankId = param.getUserBankId();
		if (userBankId < 1) {
			logger.info(loggerId + "用户很行卡信息id提供有误！");
			return ResultGenerator.genFailResult("对不起，请选择有效的很行卡！", null);
		}
		IDParam idParam = new IDParam();
		idParam.setId(userBankId);
		BaseResult<UserBankDTO> queryUserBank = userBankService
				.queryUserBank(idParam);
		if (queryUserBank.getCode() != 0) {
			logger.info(loggerId + "用户银行卡信息获取有误！");
			return ResultGenerator.genFailResult("对不起，请提供有效的银行卡！", null);
		}
		UserBankDTO userBankDTO = queryUserBank.getData();
		String realName = userBankDTO.getRealName();
		String cardNo = userBankDTO.getCardNo();
		// 生成提现单
		com.dl.member.param.UserWithdrawParam userWithdrawParam = new com.dl.member.param.UserWithdrawParam();
		userWithdrawParam.setAmount(BigDecimal.valueOf(totalAmount));
		userWithdrawParam.setCardNo(cardNo);
		userWithdrawParam.setRealName(realName);
		BaseResult<com.dl.member.dto.UserWithdrawDTO> createUserWithdraw = userAccountService
				.createUserWithdraw(userWithdrawParam);
		if (createUserWithdraw.getCode() != 0) {
			logger.info(loggerId + " 生成提现单，code=" + createUserWithdraw.getCode()
					+ " , msg=" + createUserWithdraw.getMsg());
			return ResultGenerator.genFailResult("提现失败！", null);
		}
		String orderSn = createUserWithdraw.getData().getWithdrawalSn();
		// 保存提现进度
		UserWithdrawLog userWithdrawLog = new UserWithdrawLog();
		userWithdrawLog.setLogCode(1);
		userWithdrawLog.setLogName("提现申请");
		userWithdrawLog.setLogTime(DateUtil.getCurrentTimeLong());
		userWithdrawLog.setWithdrawSn(orderSn);
		userWithdrawLogService.save(userWithdrawLog);
		return ResultGenerator.genSuccessResult("请求成功！");
	}

	@ApiOperation(value = "支付订单结果 查询 ", notes = "")
	@PostMapping("/query")
	@ResponseBody
	public BaseResult<RspOrderQueryDTO> orderquery(
			@RequestBody ReqOrderQueryParam p) {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String loggerId = "orderquery_" + System.currentTimeMillis();
		String payLogId = p.getPayLogId();
		if (StringUtils.isBlank(payLogId)) {
			return ResultGenerator.genFailResult("订单号不能为空！", null);
		}
		logger.info(loggerId + " payLogId=" + payLogId);
		PayLog payLog = payLogService.findById(Integer.parseInt(payLogId));
		if (null == payLog) {
			logger.info(loggerId + " payLogId=" + payLogId + " 没有查询到对应的订单号");
			return ResultGenerator.genFailResult("请提供有效的订单号！", null);
		}
		logger.info("查询订单:" + loggerId + " payCode:" + payLog.getPayCode());
		int isPaid = payLog.getIsPaid();
		String payCode = payLog.getPayCode();
		Integer payType = payLog.getPayType();
		if (1 == isPaid) {
			logger.info(loggerId + " 订单已支付成功");
			RspOrderQueryDTO payRQDTO = new RspOrderQueryDTO();
			payRQDTO.setPayCode(payCode);
			payRQDTO.setPayType(payType);
			if (payType == 0) {
				return ResultGenerator.genSuccessResult("订单已支付成功", payRQDTO);
			} else {
				return ResultGenerator.genSuccessResult("充值成功", payRQDTO);
			}
		}
		return ResultGenerator.genResult(PayEnums.PAY_RONGBAO_EMPTY.getcode(),
				PayEnums.PAY_RONGBAO_EMPTY.getMsg());
	}

	/**
	 * 根据payLogId查询支付信息
	 */
	@ApiOperation(value = "根据payLogId查询支付信息", notes = "根据payLogId查询支付信息")
	@PostMapping("/queryPayLogByPayLogId")
	@ResponseBody
	public BaseResult<PayLogDTO> queryPayLogByPayLogId(
			@RequestBody PayLogIdParam payLogIdParam) {
		return payLogService.queryPayLogByPayLogId(
				Integer.valueOf(payLogIdParam.getPayLogId()));
	}

	/**
	 * 根据OrderSn查询支付信息
	 */
	@ApiOperation(value = "根据orderSn查询支付信息", notes = "根据orderSn查询支付信息")
	@PostMapping("/queryPayLogByOrderSn")
	@ResponseBody
	public BaseResult<PayLogDetailDTO> queryPayLogByOrderSn(
			@RequestBody PayLogOrderSnParam payLogOrderSnParam) {
		return payLogService
				.queryPayLogByOrderSn(payLogOrderSnParam.getOrderSn());
	}

	/**
	 * 查询redis的值
	 */
	@ApiOperation(value = "查询redis的值", notes = "查询redis的值")
	@PostMapping("/queryPriceInRedis")
	@ResponseBody
	public BaseResult<PriceDTO> queryMoneyInRedis(
			@RequestBody PayLogIdParam payLogIdParam) {
		PriceDTO donationPriceDTO = new PriceDTO();
		logger.info("前端传入：" + String.valueOf(payLogIdParam.getPayLogId()));
		String donationPrice = stringRedisTemplate.opsForValue()
				.get(String.valueOf(payLogIdParam.getPayLogId()));
		logger.info("redis 取出1：" + donationPrice);
		if (!StringUtils.isEmpty(donationPrice)) {
			donationPriceDTO.setPrice(donationPrice);
		}
		stringRedisTemplate.delete(String.valueOf(payLogIdParam.getPayLogId()));
		logger.info("redis 取出2：" + donationPrice);
		return ResultGenerator.genSuccessResult("success", donationPriceDTO);
	}

	/**
	 * 第三方支付的query后的更新支付状态
	 */
	@ApiOperation(value = "第三方支付的订单query后的更新支付状态", notes = "第三方支付的query后的更新支付状态")
	@PostMapping("/timerOrderQueryScheduled")
	@ResponseBody
	public BaseResult<String> timerOrderQueryScheduled(
			@RequestBody EmptyParam emptyParam) {
		if (CHECKORDER_TASKRUN) {
			logger.info("check order pay is running ...... 请稍后重试");
			return ResultGenerator.genSuccessResult("success",
					"check cash is running ...... 请稍后重试");
		}
		CHECKORDER_TASKRUN = Boolean.TRUE;
		paymentService.timerOrderQueryScheduled();
		CHECKORDER_TASKRUN = Boolean.FALSE;
		logger.info("timerOrderQueryScheduled taskend");
		return ResultGenerator.genSuccessResult("success");
	}

	/**
	 * 第三方支付的query后的更新支付状态
	 */
	@ApiOperation(value = "第三方支付的充值query后的更新支付状态", notes = "第三方支付的query后的更新支付状态")
	@PostMapping("/timerRechargeQueryScheduled")
	@ResponseBody
	public BaseResult<String> timerRechargeQueryScheduled(
			@RequestBody EmptyParam emptyParam) {
		if (CHECKRECHARGE_TASKRUN) {
			logger.info("check recharge pay is running ...... 请稍后重试");
			return ResultGenerator.genSuccessResult("success",
					"check cash is running ...... 请稍后重试");
		}
		CHECKRECHARGE_TASKRUN = Boolean.TRUE;
		paymentService.timerRechargeQueryScheduled();
		CHECKRECHARGE_TASKRUN = Boolean.FALSE;
		logger.info("timerRechargeQueryScheduled taskend");
		return ResultGenerator.genSuccessResult("success");
	}

	/**
	 * 校验用户是否支付过
	 */
	@ApiOperation(value = "校验用户是否有过钱的交易", notes = "校验用户是否有过钱的交易")
	@PostMapping("/validUserPay")
	@ResponseBody
	public BaseResult<ValidPayDTO> validUserPay(
			@RequestBody UserIdParam userIdParam) {
		return payLogService.validUserPay(userIdParam.getUserId());
	}

	/**
	 * 支付确认页金额计算
	 */
	@ApiOperation(value = "支付确认页金额计算", notes = "支付确认页金额计算")
	@PostMapping("/unifiedPayBefore")
	@ResponseBody
	public BaseResult<UserGoPayInfoDTO> unifiedPayBefore(
			@RequestBody GoPayBeforeParam param) {
		String loggerId = "payment_unifiedPayBefore"
				+ System.currentTimeMillis();
		String payToken = param.getPayToken();
		String payCode = param.getPayCode();
		logger.info("unifiedPayBefore----2" + payCode);
		if (StringUtils.isBlank(payToken)) {
			logger.info(loggerId + "payToken值为空！");
			return ResultGenerator.genResult(PayEnums.PAY_TOKEN_EMPTY.getcode(),
					PayEnums.PAY_TOKEN_EMPTY.getMsg());
		}
		// 校验payToken的有效性
		String jsonData = stringRedisTemplate.opsForValue().get(payToken);
		if (StringUtils.isBlank(jsonData)) {
			logger.info(loggerId + "支付信息获取为空！");
			return ResultGenerator.genResult(
					PayEnums.PAY_TOKEN_EXPRIED.getcode(),
					PayEnums.PAY_TOKEN_EXPRIED.getMsg());
		}
		if (payCode != null && payCode.equals("app_offline")) {// 线下支付不清楚token
			logger.info("unifiedPayBefore----1");
		} else {
			logger.info("unifiedPayBefore----2");
			// 清除payToken
			stringRedisTemplate.delete(payToken);
		}

		UserBetPayInfoDTO betDto = null;
		try {
			betDto = JSONHelper.getSingleBean(jsonData,
					UserBetPayInfoDTO.class);
		} catch (Exception e1) {
			logger.error(loggerId + "支付信息转DIZQUserBetLottoDTO对象失败！", e1);
			return ResultGenerator.genFailResult("支付信息异常，支付失败！");
		}
		if (null == betDto) {
			return ResultGenerator.genFailResult("支付信息异常，支付失败！");
		}

		// 用户信息
		StrParam strParam = new StrParam();
		BaseResult<UserDTO> userInfoExceptPassRst = userService
				.userInfoExceptPass(strParam);
		if (userInfoExceptPassRst.getCode() != 0
				|| null == userInfoExceptPassRst.getData()) {
			return ResultGenerator.genResult(
					LottoResultEnum.OPTION_ERROR.getCode(),
					LottoResultEnum.OPTION_ERROR.getMsg());
		}
		String totalMoney = userInfoExceptPassRst.getData().getTotalMoney();
		Double userTotalMoney = Double.valueOf(totalMoney);
		Double orderMoney = betDto.getOrderMoney() != null
				? betDto.getOrderMoney()
				: 0;
		// 红包包
		BonusLimitConditionParam bonusLimitConditionParam = new BonusLimitConditionParam();
		bonusLimitConditionParam
				.setOrderMoneyPaid(BigDecimal.valueOf(orderMoney));
		BaseResult<List<UserBonusDTO>> userBonusListRst = userBonusService
				.queryValidBonusList(bonusLimitConditionParam);
		if (userBonusListRst.getCode() != 0) {
			return ResultGenerator.genResult(
					LottoResultEnum.OPTION_ERROR.getCode(),
					LottoResultEnum.OPTION_ERROR.getMsg());
		}

		boolean flag = false;
		int bonusNumber = 0;
		List<UserBonusDTO> userBonusList = userBonusListRst.getData();
		Collections.sort(userBonusList,
				Comparator.comparing(UserBonusDTO::getThisStatus)
						.thenComparing(UserBonusDTO::getThisStatus));// 根据thisStatus排序
		UserBonusDTO userBonusDto = null;
		if (!CollectionUtils.isEmpty(userBonusList)) {
			for (UserBonusDTO userBonusDTO2 : userBonusList) {// 遍历读取该订单可适配优惠券
				if ("0".equals(userBonusDTO2.getThisStatus())) {
					bonusNumber++;
				}
			}
			String bonusIdStr = param.getBonusId();
			if (StringUtils.isNotBlank(bonusIdStr)
					&& Integer.valueOf(bonusIdStr) != 0) {// 有红包id
				if (Integer.valueOf(bonusIdStr) != -1) {
					Optional<UserBonusDTO> findFirst = userBonusList.stream()
							.filter(dto -> dto.getUserBonusId()
									.equals(Integer.valueOf(bonusIdStr)))
							.findFirst();
					userBonusDto = findFirst.isPresent()
							? findFirst.get()
							: null;
					BigDecimal minamount = userBonusDto.getMinAmount() != null
							? userBonusDto.getMinAmount()
							: BigDecimal.ZERO;
					if (bonusLimitConditionParam.getOrderMoneyPaid()
							.subtract(minamount).doubleValue() < 0) {
						userBonusDto = null;
						flag = true;
					}
				}
			} else {// 没有传红包id
				// List<UserBonusDTO> userBonuses =
				// userBonusList.stream().filter(dto -> {
				// double minGoodsAmount = dto.getBonusPrice().doubleValue();
				// return orderMoney < minGoodsAmount ? false : true;
				// }).sorted((n1, n2) ->
				// n2.getBonusPrice().compareTo(n1.getBonusPrice())).collect(Collectors.toList());
				// if (userBonuses.size() > 0) {
				//// userBonusDto = userBonuses.get(0);
				// }
			}
		}
		String bonusId = userBonusDto != null
				? userBonusDto.getUserBonusId().toString()
				: null;
		Double bonusAmount = userBonusDto != null
				? userBonusDto.getBonusPrice().doubleValue()
				: 0.0;
		Double amountTemp = orderMoney - bonusAmount;// 红包扣款后的金额
		Double surplus = 0.0;
		Double thirdPartyPaid = 0.0;
		if (amountTemp < 0) {// 红包大于订单金额
			bonusAmount = orderMoney;
		} else {
			surplus = userTotalMoney > amountTemp ? amountTemp : userTotalMoney;
			thirdPartyPaid = amountTemp - surplus;
		}

		// 获取用户可用红包数量和金额
		UserBonusIdParam userBonusIdParam = new UserBonusIdParam();
		userBonusIdParam.setUserBonusId(SessionUtil.getUserId());
		// BaseResult<UserBonusDTO> userBonus =
		// userBonusService.queryUserBonusNumAndPrice(userBonusIdParam);
		// 重新缓存订单支付信息
		betDto.setBonusAmount(bonusAmount);
		betDto.setBonusId(bonusId);
		betDto.setSurplus(surplus);
		betDto.setThirdPartyPaid(thirdPartyPaid);
		betDto.setBonusNumber(bonusNumber);
		// betDto.setBonusNumber(userBonus.getData()!=null?userBonus.getData().getBonusId():0);
		String dtoJson = JSONHelper.bean2json(betDto);
		String keyStr = "bet_info_" + SessionUtil.getUserId() + "_"
				+ System.currentTimeMillis();
		String key = MD5Util.crypt(keyStr);
		stringRedisTemplate.opsForValue().set(key, dtoJson,
				ProjectConstant.BET_INFO_EXPIRE_TIME, TimeUnit.MINUTES);
		// 返回页面信息
		UserGoPayInfoDTO betPlayInfoDTO = new UserGoPayInfoDTO();
		betPlayInfoDTO.setPayToken(key);
		betPlayInfoDTO.setBonusAmount(String.format("%.2f", bonusAmount));
		betPlayInfoDTO.setBonusId(bonusId);
		betPlayInfoDTO.setBonusList(userBonusList);
		betPlayInfoDTO.setOrderMoney(String.format("%.2f", orderMoney));
		betPlayInfoDTO.setSurplus(String.format("%.2f", surplus));
		betPlayInfoDTO.setThirdPartyPaid(String.format("%.2f", thirdPartyPaid));
		betPlayInfoDTO.setLotteryClassifyId(betDto.getLotteryClassifyId() + "");
		betPlayInfoDTO.setBonusNumber(bonusNumber);
		// betPlayInfoDTO.setBonusNumber(userBonus.getData()!=null?userBonus.getData().getBonusId():0);
		if (flag) {
			betPlayInfoDTO.setBonusDesc("当前选择红包使用门槛不符合该订单！");
		}
		return ResultGenerator.genSuccessResult("success", betPlayInfoDTO);
	}

	@ApiOperation(value = "app支付调用", notes = "payToken:商品中心购买信息保存后的返回值 ，payCode：支付编码，app端微信支付为app_weixin")
	@PostMapping("/nUnifiedOrder")
	@ResponseBody
	public BaseResult<PayReturnDTO> nUnifiedOrder(@RequestBody GoPayParam param,
			HttpServletRequest request) {
		// 20181203 加入提示，不能用金钱购买
		// return ResultGenerator.genResult(PayEnums.PAY_STOP_SERVICE.getcode(),
		// PayEnums.PAY_STOP_SERVICE.getMsg());
		// 如果有订单编号 代表是二次支付 则生成一条相同单号的订单数据
		// 同时修改元数据的订单编号（此处只做修改操作，在新订单生成成功后删除修改后的订单,如果新订单生成失败还原原数据订单号）
		if ("app_rkquick".equals(param.getPayCode())) {
			return ResultGenerator.genResult(PayEnums.PAY_STOP.getcode(),
					PayEnums.PAY_STOP.getMsg());
		}

		String loggerId = "payment_nUnifiedOrder_" + System.currentTimeMillis();
		logger.info(loggerId + " int /payment/nUnifiedOrder, userId="
				+ SessionUtil.getUserId() + " ,payCode=" + param.getPayCode());
		String payToken = param.getPayToken();
		if (StringUtils.isBlank(payToken)) {
			logger.info(loggerId + "payToken值为空！");
			return ResultGenerator.genResult(PayEnums.PAY_TOKEN_EMPTY.getcode(),
					PayEnums.PAY_TOKEN_EMPTY.getMsg());
		}
		// 校验payToken的有效性
		String jsonData = stringRedisTemplate.opsForValue().get(payToken);
		logger.info("app支付jsonData=" + jsonData);
		if (StringUtils.isBlank(jsonData)) {
			logger.info(loggerId + "支付信息获取为空！");
			return ResultGenerator.genResult(
					PayEnums.PAY_TOKEN_EXPRIED.getcode(),
					PayEnums.PAY_TOKEN_EXPRIED.getMsg());
		}
		// 各支付方式最小金额检验
		if ("app_lidpay".equals(param.getPayCode())) {
			boolean bomin = lidPayService.checkMinAmount(jsonData);
			if (bomin) {
				return ResultGenerator
						.genFailResult("单笔支付仅支持大于1元，建议充值后使用账户余额下单 ");
			}
			boolean bomax = lidPayService.checkMaxAmount(jsonData);
			if (bomax) {
				return ResultGenerator
						.genFailResult("单笔支付仅支持小于10000元，建议充值后使用账户余额下单 ");
			}
		} else if ("app_apay".equals(param.getPayCode())) {
			boolean bomin = aPayService.checkMinAmount(jsonData, "9");
			if (bomin) {
				return ResultGenerator
						.genFailResult("单笔支付仅支持大于1元，建议充值后使用账户余额下单 ");
			}
			boolean bomax = aPayService.checkMaxAmount(jsonData, null);
			if (bomax) {
				return ResultGenerator
						.genFailResult("单笔支付仅支持小于10000元，建议充值后使用账户余额下单 ");
			}
		} else if ("app_awx".equals(param.getPayCode())) {
			boolean bomin = aPayService.checkMinAmount(jsonData, "6");
			if (bomin) {
				return ResultGenerator
						.genFailResult("单笔支付仅支持大于500元，建议充值后使用账户余额下单 ");
			}
			boolean bomax = aPayService.checkMaxAmount(jsonData, null);
			if (bomax) {
				return ResultGenerator
						.genFailResult("单笔支付仅支持小于10000元，建议充值后使用账户余额下单  ");
			}
		} else if ("app_awxxe".equals(param.getPayCode())) {
			boolean bomin = aPayService.checkMinAmount(jsonData, "7");
			if (bomin) {
				return ResultGenerator
						.genFailResult("单笔支付仅支持大于10元，建议充值后使用账户余额下单 ");
			}
			boolean bomax = aPayService.checkMaxAmount(jsonData, "7");
			if (bomax) {
				return ResultGenerator
						.genFailResult("单笔支付仅支持小于300元，建议充值后使用账户余额下单  ");
			}
		} else if ("app_rkquick".equals(param.getPayCode())) {
			boolean bomin = rkPayService.checkMinAmount(jsonData,
					param.getPayCode());
			if (bomin) {
				return ResultGenerator
						.genFailResult("单笔支付仅支持大于20元，建议充值后使用账户余额下单 ");
			}
			boolean bomax = rkPayService.checkMaxAmount(jsonData,
					param.getPayCode());
			if (bomax) {
				return ResultGenerator
						.genFailResult("单笔支付仅支持小于5000元，建议充值后使用账户余额下单  ");
			}
		} else if ("app_rkwap".equals(param.getPayCode())) {
			boolean bomin = rkPayService.checkMinAmount(jsonData,
					param.getPayCode());
			if (bomin) {
				return ResultGenerator
						.genFailResult("单笔支付仅支持大于80元，建议充值后使用账户余额下单 ");
			}
			boolean bomax = rkPayService.checkMaxAmount(jsonData,
					param.getPayCode());
			if (bomax) {
				return ResultGenerator
						.genFailResult("单笔支付仅支持小于3000元，建议充值后使用账户余额下单  ");
			}
		} else if ("app_jhpay".equals(param.getPayCode())) {
			boolean bomin = jhpayService.checkMinAmount(jsonData,
					param.getPayCode());
			if (bomin) {
				return ResultGenerator
						.genFailResult("单笔支付仅支持大于1元，建议充值后使用账户余额下单 ");
			}
			boolean bomax = jhpayService.checkMaxAmount(jsonData,
					param.getPayCode());
			if (bomax) {
				return ResultGenerator
						.genFailResult("单笔支付仅支持小于5000元，建议充值后使用账户余额下单  ");
			}
		}

		// 清除payToken
		stringRedisTemplate.delete(payToken);

		UserBetPayInfoDTO dto = null;
		try {
			dto = JSONHelper.getSingleBean(jsonData, UserBetPayInfoDTO.class);
		} catch (Exception e1) {
			logger.error(loggerId + "支付信息转DIZQUserBetInfoDTO对象失败！", e1);
			return ResultGenerator.genFailResult("支付信息异常，支付失败！");
		}
		if (null == dto) {
			return ResultGenerator.genFailResult("支付信息异常，支付失败！");
		}

		Integer userId = dto.getUserId();
		Integer currentId = SessionUtil.getUserId();
		if (!userId.equals(currentId)) {
			logger.info(loggerId + "支付信息不是当前用户的待支付彩票！");
			return ResultGenerator.genFailResult("支付信息异常，支付失败！");
		}
		Double orderMoney = dto.getOrderMoney();
		Integer userBonusId = StringUtils.isBlank(dto.getBonusId())
				? 0
				: Integer.valueOf(dto.getBonusId());// form
		BigDecimal ticketAmount = BigDecimal.valueOf(orderMoney);// from
		BigDecimal bonusAmount = BigDecimal.valueOf(dto.getBonusAmount());// from
																			// paytoken
		BigDecimal moneyPaid = BigDecimal
				.valueOf(orderMoney - dto.getBonusAmount());
		BigDecimal surplus = BigDecimal.valueOf(dto.getSurplus());// from
		BigDecimal thirdPartyPaid = BigDecimal.valueOf(dto.getThirdPartyPaid());

		List<UserBetDetailInfoDTO> userBetCellInfos = dto.getBetDetailInfos();
		UserBetDetailInfoDTO min = userBetCellInfos.get(0);
		if (userBetCellInfos.size() > 1) {
			min = userBetCellInfos.stream().min((cell1,
					cell2) -> cell1.getMatchTime() - cell2.getMatchTime())
					.get();
		}

		// 比赛提前1h 禁止支付
		Integer sysLimitBetTime = 3600;
		SysConfigParam sysConfigParam = new SysConfigParam();
		sysConfigParam.setBusinessId(4);
		BaseResult<SysConfigDTO> sysConfigDTOBaseResult = iSysConfigService
				.querySysConfig(sysConfigParam);
		if (sysConfigDTOBaseResult.isSuccess()) {
			sysLimitBetTime = sysConfigDTOBaseResult.getData().getValue()
					.intValue();
		}

		String strMatchTime = DateUtil.getTimeString(min.getMatchTime(),
				DateUtil.datetimeFormat);
		String strNowTime = DateUtil.getTimeString(
				DateUtil.getCurrentTimeLong(), DateUtil.datetimeFormat);

		String seconds = DateUtilPay.dateSubtractionHours(strNowTime,
				strMatchTime);
		logger.info("nUnifiedOrder()：提前售票： 比赛时间=" + min.getMatchTime() + "||"
				+ strMatchTime + "提前时间=" + sysLimitBetTime + "当前时间="
				+ DateUtil.getCurrentTimeLong() + "||" + strNowTime);
		logger.info("nUnifiedOrder()：提前售票： 是否停止售票="
				+ (Integer.valueOf(seconds) <= sysLimitBetTime));
		if (Integer.valueOf(seconds) <= sysLimitBetTime) {
			// if(min.getMatchTime() - sysLimitBetTime <= nowTime){
			return ResultGenerator.genResult(
					LotteryResultEnum.BET_TIME_LIMIT.getCode(),
					LotteryResultEnum.BET_TIME_LIMIT.getMsg());
		}

		List<TicketDetail> ticketDetails = userBetCellInfos.stream()
				.map(betCell -> {
					TicketDetail ticketDetail = new TicketDetail();
					ticketDetail.setMatch_id(betCell.getMatchId());
					ticketDetail.setChangci(betCell.getChangci());
					int matchTime = betCell.getMatchTime();
					if (matchTime > 0) {
						ticketDetail.setMatchTime(
								Date.from(Instant.ofEpochSecond(matchTime)));
					}
					ticketDetail.setMatchTeam(betCell.getMatchTeam());
					ticketDetail.setLotteryClassifyId(
							betCell.getLotteryClassifyId());
					ticketDetail.setLotteryPlayClassifyId(
							betCell.getLotteryPlayClassifyId());
					ticketDetail.setTicketData(betCell.getTicketData());
					ticketDetail.setIsDan(betCell.getIsDan());
					ticketDetail.setIssue(betCell.getPlayCode());
					ticketDetail.setFixedodds(betCell.getFixedodds());
					ticketDetail.setBetType(betCell.getBetType());
					ticketDetail.setForecastScore(betCell.getForecastScore());
					return ticketDetail;
				}).collect(Collectors.toList());
		// 余额支付
		boolean hasSurplus = false;
		if ((surplus != null && surplus.doubleValue() > 0)
				|| (bonusAmount != null && bonusAmount.doubleValue() > 0)) {
			hasSurplus = true;
		}
		// 临时添加
		boolean isSurplus = false;
		if (surplus != null && surplus.doubleValue() > 0) {
			isSurplus = true;
		}
		// 第三方支付
		boolean hasThird = false;
		if (thirdPartyPaid != null && thirdPartyPaid.doubleValue() > 0) {
			hasThird = true;
			String payCode = param.getPayCode();
			if (StringUtils.isBlank(payCode)) {
				logger.info(loggerId + "第三方支付，paycode为空~");
				return ResultGenerator.genResult(
						PayEnums.PAY_CODE_BLANK.getcode(),
						PayEnums.PAY_CODE_BLANK.getMsg());
			}
		}
		PaymentDTO paymentDto = null;
		String payName = null;
		if (hasThird) {
			// 支付方式校验
			String payCode = param.getPayCode();
			if (StringUtils.isBlank(payCode)) {
				logger.info(loggerId + "订单第三支付没有提供paycode！");
				return ResultGenerator.genFailResult("对不起，您还没有选择第三方支付！", null);
			}
			BaseResult<PaymentDTO> paymentResult = paymentService
					.queryByCode(payCode);
			if (paymentResult.getCode() != 0) {
				logger.info(loggerId + "订单第三方支付提供paycode有误！payCode=" + payCode);
				return ResultGenerator.genFailResult("请选择有效的支付方式！", null);
			}
			paymentDto = paymentResult.getData();
			payName = paymentDto.getPayName();
		}
		// order生成
		SubmitOrderParam submitOrderParam = new SubmitOrderParam();
		submitOrderParam.setTicketNum(dto.getTicketNum());
		submitOrderParam.setMoneyPaid(moneyPaid);
		submitOrderParam.setTicketAmount(ticketAmount);
		submitOrderParam.setSurplus(surplus);
		submitOrderParam.setThirdPartyPaid(thirdPartyPaid);
		submitOrderParam.setPayName(payName);
		submitOrderParam.setUserBonusId(userBonusId);
		submitOrderParam.setBonusAmount(bonusAmount);
		submitOrderParam.setOrderFrom(dto.getRequestFrom());
		int lotteryClassifyId = dto.getLotteryClassifyId();
		String lotteryClassifyIdStr = lotteryClassifyId + "";
		submitOrderParam.setLotteryClassifyId(lotteryClassifyId);
		int lotteryPlayClassifyId = dto.getLotteryPlayClassifyId();
		submitOrderParam.setLotteryPlayClassifyId(lotteryPlayClassifyId);
		submitOrderParam.setPassType(dto.getBetType());
		submitOrderParam.setPlayType("0" + dto.getPlayType());
		submitOrderParam.setBetNum(dto.getBetNum());
		submitOrderParam.setPlayTypeDetail(dto.getPlayTypeDetail());
		submitOrderParam.setCathectic(dto.getTimes());
		if (lotteryPlayClassifyId != 8 && lotteryClassifyId == 1) {
			if (ticketDetails.size() > 1) {
				Optional<TicketDetail> max = ticketDetails.stream()
						.max((detail1, detail2) -> detail1.getMatchTime()
								.compareTo(detail2.getMatchTime()));
				submitOrderParam.setMatchTime(max.get().getMatchTime());
			} else {
				submitOrderParam
						.setMatchTime(ticketDetails.get(0).getMatchTime());
			}
		}
		submitOrderParam.setForecastMoney(dto.getForecastMoney());

		submitOrderParam.setIssue(dto.getIssue());
		submitOrderParam.setTicketDetails(ticketDetails);
		submitOrderParam.set_orderSn(param.getOrderSn());
		submitOrderParam.setPayCode(param.getPayCode());
		if (StringUtil.isNotEmpty(param.getOrderSn())) {// 修改元数据订单编号并且将订单置为无效
														// 修改规则为 元订单编号+"1"
			logger.info("nUnifiedOrderNew METHOD1:=" + param.getOrderSn());
			SubmitOrderParam sparam = new SubmitOrderParam();
			sparam.set_orderSn(param.getOrderSn());
			sparam.set_orderSn_new(param.getOrderSn().concat("1"));
			sparam.setIsDelete(1);
			orderService.updateOrderToOrderSn(sparam);
			logger.info("nUnifiedOrderNew METHOD1111:=" + param.getOrderSn());
		}
		logger.info("nUnifiedOrderNew METHOD2:=" + param.getOrderSn());
		BaseResult<OrderDTO> createOrder = orderService
				.createOrder(submitOrderParam);// 创建新订单
		if (createOrder.getCode() != 0) {// 订单创建失败--还原元订单
			if (StringUtil.isNotEmpty(param.getOrderSn())) {
				logger.info("nUnifiedOrderNew METHOD3:=" + param.getOrderSn());
				SubmitOrderParam sparam = new SubmitOrderParam();
				sparam.set_orderSn_new(param.getOrderSn());
				sparam.set_orderSn(param.getOrderSn().concat("1"));
				sparam.setIsDelete(0);
				orderService.updateOrderToOrderSn(sparam);
				logger.info("nUnifiedOrderNew METHOD4:=" + param.getOrderSn());
			}
			logger.info(loggerId + "订单创建失败！");
			return ResultGenerator.genFailResult("支付失败！");
		}
		logger.info("nUnifiedOrderNew METHOD5:=" + param.getOrderSn());
		String orderId = createOrder.getData().getOrderId().toString();
		String orderSn = createOrder.getData().getOrderSn();

		if (hasSurplus) {
			// 用户余额扣除
			SurplusPayParam surplusPayParam = new SurplusPayParam();
			surplusPayParam.setOrderSn(orderSn);
			surplusPayParam.setSurplus(surplus);
			surplusPayParam.setBonusMoney(bonusAmount);
			int payType1 = 2;
			if (hasThird) {
				payType1 = 3;

			}
			surplusPayParam.setPayType(payType1);
			surplusPayParam.setMoneyPaid(surplus);
			surplusPayParam.setThirdPartName("");
			surplusPayParam.setThirdPartPaid(BigDecimal.ZERO);
			if (isSurplus) {
				BaseResult<SurplusPaymentCallbackDTO> changeUserAccountByPay = userAccountService
						.changeUserAccountByPay(surplusPayParam);
				if (changeUserAccountByPay.getCode() != 0) {
					logger.info(loggerId + "用户余额扣减失败！");
					return ResultGenerator.genFailResult("支付失败！");
				}
				// 更新余额支付信息到订单
				BigDecimal userSurplus = changeUserAccountByPay.getData()
						.getUserSurplus();
				BigDecimal userSurplusLimit = changeUserAccountByPay.getData()
						.getUserSurplusLimit();
				UpdateOrderInfoParam updateOrderInfoParam = new UpdateOrderInfoParam();
				updateOrderInfoParam.setOrderSn(orderSn);
				updateOrderInfoParam.setUserSurplus(userSurplus);
				updateOrderInfoParam.setUserSurplusLimit(userSurplusLimit);
				BaseResult<String> updateOrderInfo = orderService
						.updateOrderInfo(updateOrderInfoParam);
				if (updateOrderInfo.getCode() != 0) {
					logger.info(loggerId + "订单回写用户余额扣减详情失败！");
					BaseResult<SurplusPaymentCallbackDTO> rollbackUserAccountChangeByPay = userAccountService
							.rollbackUserAccountChangeByPay(surplusPayParam);
					logger.info(loggerId + " orderSn=" + orderSn + " , Surplus="
							+ surplus.doubleValue()
							+ " 在回滚用户余额结束！ 订单回调返回结果：status="
							+ rollbackUserAccountChangeByPay.getCode()
							+ " , message="
							+ rollbackUserAccountChangeByPay.getMsg());
					if (rollbackUserAccountChangeByPay.getCode() != 0) {
						logger.info(loggerId + " orderSn=" + orderSn
								+ " , Surplus=" + surplus.doubleValue()
								+ " 在回滚用户余额时出错！");
					}
					return ResultGenerator.genFailResult("支付失败！");
				}

			}
			if (!hasThird) {
				// 回调order,更新支付状态,余额支付成功
				UpdateOrderPayStatusParam param1 = new UpdateOrderPayStatusParam();
				param1.setPayStatus(1);
				int currentTime = DateUtil.getCurrentTimeLong();
				param1.setPayTime(currentTime);
				param1.setOrderSn(orderSn);
				param1.setPayCode("");
				param1.setPayName("");
				param1.setPaySn("");
				BaseResult<Integer> baseResult = orderService
						.updateOrderPayStatus(param1);
				logger.info(loggerId + " 订单成功状态更新回调返回结果：status="
						+ baseResult.getCode() + " , message="
						+ baseResult.getMsg() + "data=" + baseResult.getData());
				if (baseResult.getCode() != 0 && isSurplus
						&& !Integer.valueOf(1).equals(baseResult.getData())) {
					BaseResult<SurplusPaymentCallbackDTO> rollbackUserAccountChangeByPay = userAccountService
							.rollbackUserAccountChangeByPay(surplusPayParam);
					logger.info(loggerId + " orderSn=" + orderSn + " , Surplus="
							+ surplus.doubleValue()
							+ " 在订单成功状态更新回滚用户余额结束！ 订单回调返回结果：status="
							+ rollbackUserAccountChangeByPay.getCode()
							+ " , message="
							+ rollbackUserAccountChangeByPay.getMsg());
					if (rollbackUserAccountChangeByPay.getCode() != 0) {
						logger.info(loggerId + " orderSn=" + orderSn
								+ " , Surplus=" + surplus.doubleValue()
								+ " 在订单成功状态更新回滚用户余额时出错！");
					}
					return ResultGenerator.genFailResult("支付失败！");
				}
				logger.info(loggerId + "订单没有需要第三方支付金额，完全余额支付成功！");
				PayReturnDTO payReturnDTO = new PayReturnDTO();
				payReturnDTO.setOrderId(orderId);

				// 单纯余额支付的时候记录第一次支付时间
				logger.info("开始记录第一次支付时间");
				FirstPayTimeParam firstPayTimeParam = new FirstPayTimeParam();
				firstPayTimeParam.setOrderSn(orderSn);
				BaseResult<String> storeUserMoneyRst = iStoreUserMoneyService
						.recordFirstPayTime(firstPayTimeParam);
				if (storeUserMoneyRst.getCode() == 0) {
					logger.info(storeUserMoneyRst.getMsg());
				}

				// 推广活动begin
				com.dl.activity.param.StrParam strparam = new com.dl.activity.param.StrParam();
				strparam.setStr(surplus.toString());// 购彩金额 不包含优惠券
				activiService.buyLotteryRerurnReward(strparam);
				// 推广活动end

				// payReturnDTO.setLotteryClassifyId(lotteryClassifyIdStr);
				return ResultGenerator.genSuccessResult("支付成功！", payReturnDTO);
			}
		}
		// payCode处理
		String payCode = paymentDto.getPayCode();
		if ("app_weixin".equals(payCode)) {
			boolean isWechat = (param.getInnerWechat() == 1);
			Boolean openJianLian = paymentService.getJianLianIsOpen();
			if (openJianLian) {
				isWechat = Boolean.TRUE;
				param.setInnerWechat(1);
			}
			if (isWechat) {
				payCode = "app_weixin" + "_h5";
			}
		}
		int uid = SessionUtil.getUserId();
		String payIp = this.getIpAddr(request);
		PayLog payLog = super.newPayLog(uid, orderSn, thirdPartyPaid, 0,
				payCode, paymentDto.getPayName(), payIp);
		PayLog savePayLog = payLogService.savePayLog(payLog);
		if (null == savePayLog) {
			logger.info(loggerId + " payLog对象保存失败！");
			return ResultGenerator.genFailResult("请求失败！", null);
		} else {
			logger.info("paylog save succ:" + " payLogId:" + payLog.getPayIp()
					+ " paycode:" + payLog.getPayCode() + " payname:"
					+ payLog.getPayName());
		}
		// url下发后，服务器开始主动轮序订单状态
		// PayManager.getInstance().addReqQueue(orderSn,savePayLog.getPayOrderSn(),paymentDto.getPayCode());
		BaseResult payBaseResult = null;
		/*
		 * if ("app_zfb".equals(payCode)) {
		 * logger.info("支付宝支付url开始生成...isWechat:" + (param.getInnerWechat() ==
		 * 1) + " payOrderSn:" + savePayLog.getPayOrderSn()); payBaseResult =
		 * getWechatPayUrl(true, param.getInnerWechat() == 1, param.getIsH5(),
		 * 0, savePayLog, payIp, orderId, ""); logger.info("支付宝支付url生成成功 code" +
		 * payBaseResult.getCode() + " data:" + payBaseResult.getData()); } else
		 * if ("app_weixin".equals(payCode) || "app_weixin_h5".equals(payCode))
		 * { logger.info("生成微信支付url:" + "inWechat:" + (param.getInnerWechat() ==
		 * 1) + " payCode:" + savePayLog.getPayCode()); payBaseResult =
		 * getWechatPayUrl(false, param.getInnerWechat() == 1, param.getIsH5(),
		 * 0, savePayLog, payIp, orderId, lotteryClassifyIdStr); if
		 * (payBaseResult != null && payBaseResult.getData() != null) { String
		 * str = payBaseResult.getData() + ""; logger.info("生成支付url成功:" + str);
		 * } }
		 */
		if ("app_jhpay".equals(param.getPayCode())) {
			Map<String, Object> jhmap = new HashMap<>();
			jhmap.put("orderId", orderId);
			jhmap.put("orderSn", orderSn);
			jhmap.put("payLogId", savePayLog.getLogId());
			payBaseResult = ResultGenerator.genSuccessResult("succ", jhmap);
			return payBaseResult;
		}

		if ("app_lidpay".equals(paymentDto.getPayCode())) {// 华移支付
			logger.info("华移支付url:" + " payCode:" + savePayLog.getPayCode());
			payBaseResult = lidPayService.getLidPayUrl(savePayLog, orderSn,
					orderId, "支付");
			if (payBaseResult != null && payBaseResult.getData() != null) {
				String str = payBaseResult.getData() + "";
				logger.info("生成华移支付payOrderSn={},url成功 url={}:", orderSn, str);
			} else {
				logger.info("生成华移支付payOrderSn={},url失败", orderSn);
			}
		} else if ("app_apay".equals(paymentDto.getPayCode())) {// 艾支付
			logger.info("艾支付url:" + " payCode:" + savePayLog.getPayCode());
			payBaseResult = aPayService.getAPayUrl(savePayLog, orderSn, orderId,
					userId, "9", "127.0.0.1", "支付");
			if (payBaseResult != null && payBaseResult.getData() != null) {
				String str = payBaseResult.getData() + "";
				logger.info("生成艾支付payOrderSn={},url成功 url={}:", orderSn, str);
			} else {
				logger.info("生成艾支付payOrderSn={},url失败", orderSn);
			}
		} else if ("app_awx".equals(payCode)) {
			logger.info("艾支付url:" + " payCode:" + savePayLog.getPayCode());
			// String channel_id = "9";//渠道编号 微信支付(扫码):6 微信支付H5:7 支付宝支付：9
			// int iswechat = param.getInnerWechat();
			// if(iswechat==1) {
			// channel_id = "6";
			// }
			payBaseResult = aPayService.getAPayUrl(savePayLog, orderSn, orderId,
					userId, "6", "127.0.0.1", "支付");
			if (payBaseResult != null && payBaseResult.getData() != null) {
				String str = payBaseResult.getData() + "";
				logger.info("生成艾支付payOrderSn={},url成功 url={}:", orderSn, str);
			} else {
				logger.info("生成艾支付payOrderSn={},url失败", orderSn);
			}
		} else if ("app_awxxe".equals(payCode)) {
			logger.info("艾支付url:" + " payCode:" + savePayLog.getPayCode());
			// String channel_id = "9";//渠道编号 微信支付(扫码):6 微信支付H5:7 支付宝支付：9
			// int iswechat = param.getInnerWechat();
			// if(iswechat==1) {
			// channel_id = "6";
			// }
			payBaseResult = aPayService.getAPayUrl(savePayLog, orderSn, orderId,
					userId, "6", "127.0.0.1", "支付");
			if (payBaseResult != null && payBaseResult.getData() != null) {
				String str = payBaseResult.getData() + "";
				logger.info("生成艾支付payOrderSn={},url成功 url={}:", orderSn, str);
			} else {
				logger.info("生成艾支付payOrderSn={},url失败", orderSn);
			}
		} else if ("app_rkquick".equals(payCode)) {
			logger.info(
					"Q多多网银快捷支付url:" + " payCode:" + savePayLog.getPayCode());
			payBaseResult = rkPayService.getRkPayQuickUrl(savePayLog, "NORMAL",
					orderSn, orderId, "支付", "", "", "", "", "");
			if (payBaseResult != null && payBaseResult.getData() != null) {
				String str = payBaseResult.getData() + "";
				logger.info("生成Q多多网银快捷支付payOrderSn={},url成功 url={}:", orderSn,
						str);
			} else {
				logger.info("生成Q多多网银快捷支付payOrderSn={},url失败", orderSn);
			}
		} else if ("app_rkwap".equals(payCode)) {
			logger.info("Q多多支付宝支付url:" + " payCode:" + savePayLog.getPayCode());
			payBaseResult = rkPayService.getRkPayWapUrl(savePayLog, orderSn,
					orderId, "支付");
			if (payBaseResult != null && payBaseResult.getData() != null) {
				String str = payBaseResult.getData() + "";
				logger.info("生成Q多多支付宝支付payOrderSn={},url成功 url={}:", orderSn,
						str);
			} else {
				logger.info("生成Q多多支付宝支付payOrderSn={},url失败", orderSn);
			}
		}
		logger.info(loggerId + " result: code=" + payBaseResult.getCode()
				+ " , msg=" + payBaseResult.getMsg());
		logger.info("支付成功后：" + JSONUtils.valueToString(payBaseResult));

		// 包含了第三方支付的时候记录第一次支付时间
		// if(payBaseResult.getCode() == 0){
		// logger.info("开始记录第一次支付时间");
		// FirstPayTimeParam firstPayTimeParam = new FirstPayTimeParam();
		// firstPayTimeParam.setOrderSn(orderSn);
		// BaseResult<String> storeUserMoneyRst =
		// iStoreUserMoneyService.recordFirstPayTime(firstPayTimeParam);
		// if(storeUserMoneyRst.getCode() == 0){
		// logger.info(storeUserMoneyRst.getMsg());
		// }
		// }

		return payBaseResult;
	}

	@ApiOperation(value = "财务商户余额查询", notes = "payCode：支付编码，app端微信支付为app_weixin")
	@PostMapping("/getShMoney")
	@ResponseBody
	public BaseResult<RspOrderQueryDTO> getShMoney(
			@RequestBody com.dl.shop.payment.param.StrParam emptyParam) {
		BaseResult<RspOrderQueryDTO> payBaseResult = rkPayService
				.getShMoney(emptyParam);
		if (payBaseResult != null) {
			return payBaseResult;
		} else {
			return ResultGenerator.genFailResult("参数异常");
		}
	}

	@ApiOperation(value = "财务商户充值调用——无流水", notes = "payCode：支付编码，app端微信支付为app_weixin")
	@PostMapping("/rechargeCw")
	@ResponseBody
	public BaseResult<Object> rechargeForAppByCw(
			@RequestBody RechargeParam param, HttpServletRequest request) {
		double totalAmount = param.getTotalAmount();
		String orderSn = SNGenerator
				.nextSN(SNBusinessCodeEnum.RECHARGE_SN.getCode());
		if (totalAmount < 20) {
			return ResultGenerator.genFailResult("单笔充值金额不能低于20元 ");
		}
		if (totalAmount > 5000) {
			return ResultGenerator.genFailResult("单笔充值金额不能超过5000元 ");
		}
		PayLog payLog = new PayLog();
		payLog.setOrderAmount(BigDecimal.valueOf(totalAmount));
		BaseResult payBaseResult = rkPayService.getRkPayQuickUrl(payLog,
				"NORMAL", orderSn, orderSn, "充值", "", "", "", "", "");
		if (payBaseResult != null) {
			return payBaseResult;
		} else {
			return ResultGenerator.genFailResult("参数异常");
		}
	}
	/**
	 * 支付宝授权充值
	 * 
	 * @param param
	 * @param request
	 * @return
	 */
	public BaseResult<Object> rechargeForAppNew(RechargeParam param,
			HttpServletRequest request) {
		String loggerId = "rechargeForAppNew_" + System.currentTimeMillis();
		String orderSn = param.getOrderSn();
		PayLog savePayLog = payLogService.findPayLogByOrderSn(orderSn);
		BaseResult payBaseResult = null;
		if ("app_jhpay".equals(param.getPayCode())) {
			logger.info("聚合支付宝支付url:" + " payCode:" + savePayLog.getPayCode());
			payBaseResult = jhpayService.getZFBPayUrl(savePayLog, orderSn,
					orderSn, "充值", param.getUserId());
			if (payBaseResult != null && payBaseResult.getData() != null) {
				String str = payBaseResult.getData() + "";
				logger.info("生成聚合支付宝支付payOrderSn={},url成功 url={}:", orderSn,
						str);
			} else {
				logger.info("生成聚合支付宝支付payOrderSn={},url失败", orderSn);
			}
		}

		// 处理支付失败的情况
		if (null == payBaseResult || payBaseResult.getCode() != 0) {
			// 充值失败逻辑
			// 更改充值单状态
			UpdateUserRechargeParam updateUserParams = new UpdateUserRechargeParam();
			updateUserParams.setPaymentCode(savePayLog.getPayCode());
			updateUserParams.setPaymentId(savePayLog.getLogId() + "");
			updateUserParams.setPaymentName(savePayLog.getPayName());
			updateUserParams.setPayTime(DateUtil.getCurrentTimeLong());
			updateUserParams.setRechargeSn(orderSn);
			updateUserParams.setStatus("2");
			BaseResult<String> baseResult = userRechargeService
					.updateReCharege(updateUserParams);
			logger.info(
					loggerId + " 充值失败更改充值单返回信息：status=" + baseResult.getCode()
							+ " , message=" + baseResult.getMsg());
			if (baseResult.getCode() == 0) {
				// 更改流水信息
				try {
					PayLog updatePayLog = new PayLog();
					updatePayLog.setLogId(savePayLog.getLogId());
					updatePayLog.setIsPaid(0);
					updatePayLog.setPayMsg(baseResult.getMsg());
					payLogService.updatePayMsg(updatePayLog);
				} catch (Exception e) {
					logger.error(loggerId + "paylogid=" + savePayLog.getLogId()
							+ " , paymsg=" + baseResult.getMsg() + "保存失败记录时出错",
							e);
				}
			}
		}
		if (payBaseResult != null) {
			logger.info(loggerId + " result: code=" + payBaseResult.getCode()
					+ " , msg=" + payBaseResult.getMsg());
			return payBaseResult;
		} else {
			return ResultGenerator.genFailResult("参数异常");
		}
	}

	public BaseResult<Object> nUnifiedOrderNew(GoPayParam param,
			HttpServletRequest request) {
		String loggerId = "payment_nUnifiedOrder_" + System.currentTimeMillis();
		String orderSn = param.getOrderSn();
		PayLog savePayLog = payLogService.findPayLogByOrderSn(orderSn);
		BaseResult payBaseResult = null;
		if ("app_jhpay".equals(param.getPayCode())) {
			logger.info("聚合支付宝支付url:" + " payCode:" + savePayLog.getPayCode());
			payBaseResult = jhpayService.getZFBPayUrl(savePayLog, orderSn,
					orderSn, "充值", param.getUserId());
			if (payBaseResult != null && payBaseResult.getData() != null) {
				String str = payBaseResult.getData() + "";
				logger.info("生成聚合支付宝支付payOrderSn={},url成功 url={}:", orderSn,
						str);
			} else {
				logger.info("生成聚合支付宝支付payOrderSn={},url失败", orderSn);
			}
		}
		//
		// if(payBaseResult.getCode() == 0){
		// logger.info("开始记录第一次支付时间");
		// FirstPayTimeParam firstPayTimeParam = new FirstPayTimeParam();
		// firstPayTimeParam.setOrderSn(orderSn);
		// BaseResult<String> storeUserMoneyRst =
		// iStoreUserMoneyService.recordFirstPayTime(firstPayTimeParam);
		// if(storeUserMoneyRst.getCode() == 0){
		// logger.info(storeUserMoneyRst.getMsg());
		// }
		// }

		return payBaseResult;
	}
	
	public void smkPayNotify(String orderSn,String status)  {
		PayLog payLog = payLogService.findPayLogByOrderSn(orderSn);
		if (payLog == null) {
			return;
		} 
		int isPaid = payLog.getIsPaid();
		if (isPaid == 1) {
			return;
		}
		int payType = payLog.getPayType();
		String payCode = payLog.getPayCode();
		RspOrderQueryEntity rspOrderEntikty = new RspOrderQueryEntity();
		rspOrderEntikty.setResult_code(status);
		rspOrderEntikty.setPayCode(payCode);
		rspOrderEntikty.setType(RspOrderQueryEntity.TYPE_SMKPAY);
		rspOrderEntikty.setTrade_status(status);
		if (payType == 0) {
			paymentService.orderOptions(payLog, rspOrderEntikty);
		} else {
			paymentService.rechargeOptions(payLog, rspOrderEntikty);
		}
	}

}
