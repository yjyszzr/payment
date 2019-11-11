package com.dl.shop.payment.model;

import javax.persistence.*;

@Table(name = "dl_user_withdraw_log")
public class UserWithdrawLog {
    /**
     * 提现进度ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 提现单号
     */
    @Column(name = "withdraw_sn")
    private String withdrawSn;

    /**
     * 执行时间
     */
    @Column(name = "log_time")
    private Integer logTime;

    /**
     * 进度编号
     */
    @Column(name = "log_code")
    private Integer logCode;

    /**
     * 进度名称
     */
    @Column(name = "log_name")
    private String logName;

    /**
     * 获取提现进度ID
     *
     * @return id - 提现进度ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * 设置提现进度ID
     *
     * @param id 提现进度ID
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取提现单号
     *
     * @return withdraw_sn - 提现单号
     */
    public String getWithdrawSn() {
        return withdrawSn;
    }

    /**
     * 设置提现单号
     *
     * @param withdrawSn 提现单号
     */
    public void setWithdrawSn(String withdrawSn) {
        this.withdrawSn = withdrawSn;
    }

    /**
     * 获取执行时间
     *
     * @return log_time - 执行时间
     */
    public Integer getLogTime() {
        return logTime;
    }

    /**
     * 设置执行时间
     *
     * @param logTime 执行时间
     */
    public void setLogTime(Integer logTime) {
        this.logTime = logTime;
    }

    /**
     * 获取进度编号
     *
     * @return log_code - 进度编号
     */
    public Integer getLogCode() {
        return logCode;
    }

    /**
     * 设置进度编号
     *
     * @param logCode 进度编号
     */
    public void setLogCode(Integer logCode) {
        this.logCode = logCode;
    }

    /**
     * 获取进度名称
     *
     * @return log_name - 进度名称
     */
    public String getLogName() {
        return logName;
    }

    /**
     * 设置进度名称
     *
     * @param logName 进度名称
     */
    public void setLogName(String logName) {
        this.logName = logName;
    }
}