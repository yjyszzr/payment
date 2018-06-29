package com.dl.shop.payment.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "dl_pay_bank_record")
public class PayBankRecordModel {
	 @Id
	 @Column(name = "id")
	 private Integer id;
	 
	 @Column(name = "user_id")
     private Integer userId;
	 
     @Column(name = "bank_card_no")
     private String bankCardNo;
     
     @Column(name = "user_name")
     private String userName;
     
     @Column(name = "cert_no")
     private String certNo;
     
     @Column(name = "phone")
     private String phone;
     
     @Column(name = "bank_type")
     private Integer bankType;

     @Column(name = "cvn2")
     private String cvn2;

     @Column(name = "valid_date")
     private String validDate;
     
     @Column(name = "last_time")
     private Integer lastTime;
     
     @Column(name = "bank_name")
     private String bankName;
     
     @Column(name = "paylog_id")
     private Integer payLogId;
     
     @Column(name = "is_paid")
     private Integer isPaid;
     
	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public Integer getPayLogId() {
		return payLogId;
	}

	public void setPayLogId(Integer payLogId) {
		this.payLogId = payLogId;
	}

	public Integer getIsPaid() {
		return isPaid;
	}

	public void setIsPaid(Integer isPaid) {
		this.isPaid = isPaid;
	}

	public String getCvn2() {
		return cvn2;
	}

	public void setCvn2(String cvn2) {
		this.cvn2 = cvn2;
	}

	public String getValidDate() {
		return validDate;
	}

	public void setValidDate(String validDate) {
		this.validDate = validDate;
	}

	public Integer getLastTime() {
		return lastTime;
	}

	public void setLastTime(Integer lastTime) {
		this.lastTime = lastTime;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getBankCardNo() {
		return bankCardNo;
	}

	public void setBankCardNo(String bankCardNo) {
		this.bankCardNo = bankCardNo;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getCertNo() {
		return certNo;
	}

	public void setCertNo(String certNo) {
		this.certNo = certNo;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Integer getBankType() {
		return bankType;
	}

	public void setBankType(Integer bankType) {
		this.bankType = bankType;
	}
}
