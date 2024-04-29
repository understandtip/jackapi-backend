package com.jackqiu.jackapigateway;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class JackapiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(JackapiGatewayApplication.class, args);
	}

}
