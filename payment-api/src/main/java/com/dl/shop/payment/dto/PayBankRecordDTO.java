package com.dl.shop.payment.dto;

import lombok.Data;

@Data
public class PayBankRecordDTO {
	 private Integer recordId;
     private Integer userId;
     private String bankCardNo;
     private String userName;
     private String certNo;
     private String phone;
     private int bankType;
     private String cvn2;
     private String vaildDate;
     private String bankName;
     private Integer lastTime;
     private String message;
}
