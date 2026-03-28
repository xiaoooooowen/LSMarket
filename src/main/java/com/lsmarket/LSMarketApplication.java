package com.lsmarket;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan("com.lsmarket.mapper")
@EnableScheduling
@SpringBootApplication
public class LSMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(LSMarketApplication.class, args);
    }

}

