package com.nzhk.wxg;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan(basePackages = {"com.nzhk.wxg.**"} )
@MapperScan(value = "com.nzhk.wxg.mapper")
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class WxgApplication {

    public static void main(String[] args) {
        SpringApplication.run(WxgApplication.class, args);
    }

}
