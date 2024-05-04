package com.study.liao;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
@MapperScan("com.study.liao.dao")
@EnableAsync
@EnableTransactionManagement
@EnableScheduling
@SpringBootApplication
public class LiaoPanApplication {
    public static void main(String[] args) {
        SpringApplication.run(LiaoPanApplication.class, args);
    }
}
