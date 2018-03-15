package com.dl.shop.payment.model;

import java.math.BigDecimal;
import javax.persistence.*;

@Table(name = "dl_pay_log")
public class PayLog {
    @Id
    @Column(name = "log_id")
    private Integer logId;

    /**
     * 用户编号
     */
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "order_sn")
    private String orderSn;

    @Column(name = "parent_sn")
    private String parentSn;

    /**
     * 订单的所支付的总金额
     */
    @Column(name = "order_amount")
    private BigDecimal orderAmount;

    /**
     * 支付方式
     */
    @Column(name = "pay_code")
    private String payCode;

    /**
     * 支付方式名称
     */
    @Column(name = "pay_name")
    private String payName;

    /**
     * 添加记录时间
     */
    @Column(name = "add_time")
    private Integer addTime;

    /**
     * 更新记录时间
     */
    @Column(name = "last_time")
    private Integer lastTime;

    /**
     * 支付类型：0-订单支付 1-充值
     */
    @Column(name = "pay_type")
    private Integer payType;

    /**
     * 是否已经支付(0-未支付;1-已支付)
     */
    @Column(name = "is_paid")
    private Integer isPaid;

    /**
     * 支付时间
     */
    @Column(name = "pay_time")
    private Integer payTime;

    @Column(name = "trade_no")
    private String tradeNo;

    @Column(name = "pay_ip")
    private String payIp;

    @Column(name = "pay_msg")
    private String payMsg;

    /**
     * @return log_id
     */
    public Integer getLogId() {
        return logId;
    }

    /**
     * @param logId
     */
    public void setLogId(Integer logId) {
        this.logId = logId;
    }

    /**
     * 获取用户编号
     *
     * @return user_id - 用户编号
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * 设置用户编号
     *
     * @param userId 用户编号
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * @return order_sn
     */
    public String getOrderSn() {
        return orderSn;
    }

    /**
     * @param orderSn
     */
    public void setOrderSn(String orderSn) {
        this.orderSn = orderSn;
    }

    /**
     * @return parent_sn
     */
    public String getParentSn() {
        return parentSn;
    }

    /**
     * @param parentSn
     */
    public void setParentSn(String parentSn) {
        this.parentSn = parentSn;
    }

    /**
     * 获取订单的所支付的总金额
     *
     * @return order_amount - 订单的所支付的总金额
     */
    public BigDecimal getOrderAmount() {
        return orderAmount;
    }

    /**
     * 设置订单的所支付的总金额
     *
     * @param orderAmount 订单的所支付的总金额
     */
    public void setOrderAmount(BigDecimal orderAmount) {
        this.orderAmount = orderAmount;
    }

    /**
     * 获取支付方式
     *
     * @return pay_code - 支付方式
     */
    public String getPayCode() {
        return payCode;
    }

    /**
     * 设置支付方式
     *
     * @param payCode 支付方式
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
     * 获取添加记录时间
     *
     * @return add_time - 添加记录时间
     */
    public Integer getAddTime() {
        return addTime;
    }

    /**
     * 设置添加记录时间
     *
     * @param addTime 添加记录时间
     */
    public void setAddTime(Integer addTime) {
        this.addTime = addTime;
    }

    /**
     * 获取更新记录时间
     *
     * @return last_time - 更新记录时间
     */
    public Integer getLastTime() {
        return lastTime;
    }

    /**
     * 设置更新记录时间
     *
     * @param lastTime 更新记录时间
     */
    public void setLastTime(Integer lastTime) {
        this.lastTime = lastTime;
    }

    /**
     * 获取支付类型：0-订单支付 1-充值
     *
     * @return pay_type - 支付类型：0-订单支付 1-充值
     */
    public Integer getPayType() {
        return payType;
    }

    /**
     * 设置支付类型：0-订单支付 1-充值
     *
     * @param payType 支付类型：0-订单支付 1-充值
     */
    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    /**
     * 获取是否已经支付(0-未支付;1-已支付)
     *
     * @return is_paid - 是否已经支付(0-未支付;1-已支付)
     */
    public Integer getIsPaid() {
        return isPaid;
    }

    /**
     * 设置是否已经支付(0-未支付;1-已支付)
     *
     * @param isPaid 是否已经支付(0-未支付;1-已支付)
     */
    public void setIsPaid(Integer isPaid) {
        this.isPaid = isPaid;
    }

    /**
     * 获取支付时间
     *
     * @return pay_time - 支付时间
     */
    public Integer getPayTime() {
        return payTime;
    }

    /**
     * 设置支付时间
     *
     * @param payTime 支付时间
     */
    public void setPayTime(Integer payTime) {
        this.payTime = payTime;
    }

    /**
     * @return trade_no
     */
    public String getTradeNo() {
        return tradeNo;
    }

    /**
     * @param tradeNo
     */
    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    /**
     * @return pay_ip
     */
    public String getPayIp() {
        return payIp;
    }

    /**
     * @param payIp
     */
    public void setPayIp(String payIp) {
        this.payIp = payIp;
    }

    /**
     * @return pay_msg
     */
    public String getPayMsg() {
        return payMsg;
    }

    /**
     * @param payMsg
     */
    public void setPayMsg(String payMsg) {
        this.payMsg = payMsg;
    }
}