package com.dl.shop.payment.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "dl_bank_ubey_code")
public class BankUbeyCodeModel {
	 @Id
	 @Column(name = "id")
	 private Integer id;
	 
	 @Column(name = "image")
     private String image;
	 
     @Column(name = "code")
     private String code;
     
     @Column(name = "name")
     private String name;
     
     @Column(name = "is_show")
     private Integer isShow;
     
     @Column(name = "type")
     private Integer type;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getIsShow() {
		return isShow;
	}

	public void setIsShow(Integer isShow) {
		this.isShow = isShow;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

 
     
     
      
}
