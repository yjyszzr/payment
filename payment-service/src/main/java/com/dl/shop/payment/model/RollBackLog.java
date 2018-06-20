package com.dl.shop.payment.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "dl_rollback_log")
public class RollBackLog {
	 @Id
	 @Column(name = "log_id")
	 private Integer id;
	 
	 @Column(name = "pay_log_sn")
	 private String payLogSn;
	 
	 @Column(name = "amt")
	 private String amt;
	 
	 @Column(name = "req")
	 private String req;
	 
	 @Column(name = "rsp")
	 private String rsp;
	 
	 @Column(name = "time")
	 private String time;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getPayLogSn() {
		return payLogSn;
	}

	public void setPayLogSn(String payLogSn) {
		this.payLogSn = payLogSn;
	}

	public String getAmt() {
		return amt;
	}

	public void setAmt(String amt) {
		this.amt = amt;
	}

	public String getReq() {
		return req;
	}

	public void setReq(String req) {
		this.req = req;
	}

	public String getRsp() {
		return rsp;
	}

	public void setRsp(String rsp) {
		this.rsp = rsp;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
}
