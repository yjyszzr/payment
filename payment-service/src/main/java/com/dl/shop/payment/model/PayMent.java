package com.dl.shop.payment.model;

import javax.persistence.*;

import io.swagger.annotations.ApiModelProperty;

@Table(name = "dl_payment")
public class PayMent {
    /**
     * 编号
     */
    @Id
    @Column(name = "pay_id")
    private Integer payId;

    /**
     * 支付方式代码
     */
    @Column(name = "pay_code")
    private String payCode;

    /**
     * 支付方式名称
     */
    @Column(name = "pay_name")
    private String payName;

    /**
     * 支付类型
     */
    @Column(name = "pay_type")
    private Integer payType;

    /**
     * 费用
     */
    @Column(name = "pay_fee")
    private String payFee;

    /**
     * 排序
     */
    @Column(name = "pay_sort")
    private Integer paySort;

    /**
     * 是否启用
     */
    @Column(name = "is_enable")
    private Integer isEnable;
    /**
     * 是否固额
     */
    @Column(name = "is_readonly")
    private Integer isReadonly;
    /**
     * 是否固额
     */
    @Column(name = "read_money")
    private String readMoney;
    
    @Column(name = "pay_title")
    private String payTitle;

    @Column(name = "pay_img")
    private String payImg;
    @Column(name = "is_h5")
    private String isH5;
    @Column(name = "pay_url")
	private String payUrl;
    @Column(name = "pay_cn")
	private String payCn;
	@Column(name = "pay_st")
	private Integer paySt;
	
    public String getPayCn() {
		return payCn;
	}

	public void setPayCn(String payCn) {
		this.payCn = payCn;
	}

	public Integer getPaySt() {
		return paySt;
	}

	public void setPaySt(Integer paySt) {
		this.paySt = paySt;
	}

	public String getPayUrl() {
		return payUrl;
	}

	public void setPayUrl(String payUrl) {
		this.payUrl = payUrl;
	}

	public String getIsH5() {
		return isH5;
	}

	public void setIsH5(String isH5) {
		this.isH5 = isH5;
	}

	/**
     * 支付配置
     */
    @Column(name = "pay_config")
    private String payConfig;

    /**
     * 描述
     */
    @Column(name = "pay_desc")
    private String payDesc;

    /**
     * 获取编号
     *
     * @return pay_id - 编号
     */
    public Integer getPayId() {
        return payId;
    }

    /**
     * 设置编号
     *
     * @param payId 编号
     */
    public void setPayId(Integer payId) {
        this.payId = payId;
    }

    /**
     * 获取支付方式代码
     *
     * @return pay_code - 支付方式代码
     */
    public String getPayCode() {
        return payCode;
    }

    public Integer getIsReadonly() {
		return isReadonly;
	}

	public void setIsReadonly(Integer isReadonly) {
		this.isReadonly = isReadonly;
	}

	public String getReadMoney() {
		return readMoney;
	}

	public void setReadMoney(String readMoney) {
		this.readMoney = readMoney;
	}

	/**
     * 设置支付方式代码
     *
     * @param payCode 支付方式代码
     */
    public void setPayCode(String payCode) {
        this.payCode = payCode;
    }

    /**
     * 获取支付方式名称
     *
     * @return pay_name - 支付方式名称
     */
    public String getPayName() {
        return payName;
    }

    /**
     * 设置支付方式名称
     *
     * @param payName 支付方式名称
     */
    public void setPayName(String payName) {
        this.payName = payName;
    }

    /**
     * 获取支付类型
     *
     * @return pay_type - 支付类型
     */
    public Integer getPayType() {
        return payType;
    }

    /**
     * 设置支付类型
     *
     * @param payType 支付类型
     */
    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    /**
     * 获取费用
     *
     * @return pay_fee - 费用
     */
    public String getPayFee() {
        return payFee;
    }

    /**
     * 设置费用
     *
     * @param payFee 费用
     */
    public void setPayFee(String payFee) {
        this.payFee = payFee;
    }

    /**
     * 获取排序
     *
     * @return pay_sort - 排序
     */
    public Integer getPaySort() {
        return paySort;
    }

    /**
     * 设置排序
     *
     * @param paySort 排序
     */
    public void setPaySort(Integer paySort) {
        this.paySort = paySort;
    }

    /**
     * 获取是否启用
     *
     * @return is_enable - 是否启用
     */
    public Integer getIsEnable() {
        return isEnable;
    }

    /**
     * 设置是否启用
     *
     * @param isEnable 是否启用
     */
    public void setIsEnable(Integer isEnable) {
        this.isEnable = isEnable;
    }

    /**
     * @return pay_title
     */
    public String getPayTitle() {
        return payTitle;
    }

    /**
     * @param payTitle
     */
    public void setPayTitle(String payTitle) {
        this.payTitle = payTitle;
    }

    /**
     * @return pay_img
     */
    public String getPayImg() {
        return payImg;
    }

    /**
     * @param payImg
     */
    public void setPayImg(String payImg) {
        this.payImg = payImg;
    }

    /**
     * 获取支付配置
     *
     * @return pay_config - 支付配置
     */
    public String getPayConfig() {
        return payConfig;
    }

    /**
     * 设置支付配置
     *
     * @param payConfig 支付配置
     */
    public void setPayConfig(String payConfig) {
        this.payConfig = payConfig;
    }

    /**
     * 获取描述
     *
     * @return pay_desc - 描述
     */
    public String getPayDesc() {
        return payDesc;
    }

    /**
     * 设置描述
     *
     * @param payDesc 描述
     */
    public void setPayDesc(String payDesc) {
        this.payDesc = payDesc;
    }
}