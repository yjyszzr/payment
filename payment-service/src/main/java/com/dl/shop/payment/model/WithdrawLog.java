package com.dl.shop.payment.model;

import javax.persistence.*;

@Table(name = "dl_withdraw_log")
public class WithdrawLog {
    /**
     * 提现进度ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

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
     * 进度描述
     */
    @Column(name = "log_desc")
    private String logDesc;

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

    /**
     * 获取进度描述
     *
     * @return log_desc - 进度描述
     */
    public String getLogDesc() {
        return logDesc;
    }

    /**
     * 设置进度描述
     *
     * @param logDesc 进度描述
     */
    public void setLogDesc(String logDesc) {
        this.logDesc = logDesc;
    }
}