package com.flightbooking.stripepayment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class StripepaymentApplication {

	public static void main(String[] args) {
		SpringApplication.run(StripepaymentApplication.class, args);
	}

}
