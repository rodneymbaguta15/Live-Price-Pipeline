package com.live_price_pipeline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class LivePricePipelineApplication {

	public static void main(String[] args) {
		SpringApplication.run(LivePricePipelineApplication.class, args);
        System.out.println("Live Price Pipeline Application started successfully...");
	}

}
