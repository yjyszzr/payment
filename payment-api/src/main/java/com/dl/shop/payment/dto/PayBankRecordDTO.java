package com.dl.shop.payment.dto;

import lombok.Data;

@Data
public class PayBankRecordDTO {
	 private Integer recordId;
     private String message;
     private Integer lastTime;
}
