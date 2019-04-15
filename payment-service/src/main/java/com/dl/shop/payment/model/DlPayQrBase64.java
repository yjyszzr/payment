package com.dl.shop.payment.model;

import javax.persistence.*;

@Table(name = "dl_pay_qr_base64")
public class DlPayQrBase64 {
    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 支付流水号
     */
    @Column(name = "pay_order_sn")
    private String payOrderSn;

    /**
     * base64内容
     */
    @Column(name = "base64_content")
    private String base64Content;

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
     * 获取支付流水号
     *
     * @return payOrderSn - 支付流水号
     */
    public String getPayordersn() {
        return payOrderSn;
    }

    /**
     * 设置支付流水号
     *
     * @param payordersn 支付流水号
     */
    public void setPayordersn(String payOrderSn) {
        this.payOrderSn = payOrderSn;
    }

    /**
     * 获取base64内容
     *
     * @return base64_content - base64内容
     */
    public String getBase64Content() {
        return base64Content;
    }

    /**
     * 设置base64内容
     *
     * @param base64Content base64内容
     */
    public void setBase64Content(String base64Content) {
        this.base64Content = base64Content;
    }
}