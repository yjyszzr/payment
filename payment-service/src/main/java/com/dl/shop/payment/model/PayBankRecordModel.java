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
     private int bankType;

     
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

	public int getBankType() {
		return bankType;
	}

	public void setBankType(int bankType) {
		this.bankType = bankType;
	}
}
