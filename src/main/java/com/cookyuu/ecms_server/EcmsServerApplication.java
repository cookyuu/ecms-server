package com.cookyuu.ecms_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableCaching
@SpringBootApplication
public class EcmsServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcmsServerApplication.class, args);
	}

}
