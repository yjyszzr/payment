package com.dl.shop.payment;

import com.dl.base.configurer.FeignConfiguration;
import com.dl.base.configurer.RestTemplateConfig;
import com.dl.base.configurer.WebMvcConfigurer;
import com.dl.shop.payment.configurer.Swagger2;
import com.dl.shop.payment.core.ProjectConstant;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({RestTemplateConfig.class, Swagger2.class, WebMvcConfigurer.class, FeignConfiguration.class})
@MapperScan(ProjectConstant.MAPPER_PACKAGE)
@EnableEurekaClient
@EnableFeignClients({"com.dl.api","com.dl.member.api"})
public class PaymentServiceApplication {
	
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
