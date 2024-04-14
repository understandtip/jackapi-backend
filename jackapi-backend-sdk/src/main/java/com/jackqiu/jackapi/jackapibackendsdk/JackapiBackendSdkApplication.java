package com.jackqiu.jackapi.jackapibackendsdk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

//@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
@SpringBootApplication
public class JackapiBackendSdkApplication {

    public static void main(String[] args) {
        SpringApplication.run(JackapiBackendSdkApplication.class, args);
    }

}
