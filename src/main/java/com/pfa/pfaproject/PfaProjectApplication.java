package com.pfa.pfaproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class PfaProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(PfaProjectApplication.class, args);
    }

}
