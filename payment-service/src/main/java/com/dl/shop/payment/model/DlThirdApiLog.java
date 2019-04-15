package com.dl.shop.payment.model;

import java.util.Date;
import javax.persistence.*;

@Table(name = "dl_third_api_log")
public class DlThirdApiLog {
    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 接口名称
     */
    @Column(name = "api_name")
    private String apiName;

    /**
     * 接口类型：1融宝2银河3聚合4阿里大于5河南出票
     */
    @Column(name = "api_type")
    private Integer apiType;

    /**
     * 请求备注
     */
    @Column(name = "api_desc")
    private String apiDesc;

    /**
     * 操作时间
     */
    @Column(name = "option_time")
    private Date optionTime;

    /**
     * 请求参数
     */
    @Column(name = "api_param")
    private String apiParam;

    /**
     * 请求结果
     */
    @Column(name = "api_result")
    private String apiResult;

    /**
     * 获取ID
     *
     * @return id - ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * 设置ID
     *
     * @param id ID
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取接口名称
     *
     * @return api_name - 接口名称
     */
    public String getApiName() {
        return apiName;
    }

    /**
     * 设置接口名称
     *
     * @param apiName 接口名称
     */
    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    /**
     * 获取接口类型：1融宝2银河3聚合4阿里大于5河南出票
     *
     * @return api_type - 接口类型：1融宝2银河3聚合4阿里大于5河南出票
     */
    public Integer getApiType() {
        return apiType;
    }

    /**
     * 设置接口类型：1融宝2银河3聚合4阿里大于5河南出票
     *
     * @param apiType 接口类型：1融宝2银河3聚合4阿里大于5河南出票
     */
    public void setApiType(Integer apiType) {
        this.apiType = apiType;
    }

    /**
     * 获取请求备注
     *
     * @return api_desc - 请求备注
     */
    public String getApiDesc() {
        return apiDesc;
    }

    /**
     * 设置请求备注
     *
     * @param apiDesc 请求备注
     */
    public void setApiDesc(String apiDesc) {
        this.apiDesc = apiDesc;
    }

    /**
     * 获取操作时间
     *
     * @return option_time - 操作时间
     */
    public Date getOptionTime() {
        return optionTime;
    }

    /**
     * 设置操作时间
     *
     * @param optionTime 操作时间
     */
    public void setOptionTime(Date optionTime) {
        this.optionTime = optionTime;
    }

    /**
     * 获取请求参数
     *
     * @return api_param - 请求参数
     */
    public String getApiParam() {
        return apiParam;
    }

    /**
     * 设置请求参数
     *
     * @param apiParam 请求参数
     */
    public void setApiParam(String apiParam) {
        this.apiParam = apiParam;
    }

    /**
     * 获取请求结果
     *
     * @return api_result - 请求结果
     */
    public String getApiResult() {
        return apiResult;
    }

    /**
     * 设置请求结果
     *
     * @param apiResult 请求结果
     */
    public void setApiResult(String apiResult) {
        this.apiResult = apiResult;
    }
}