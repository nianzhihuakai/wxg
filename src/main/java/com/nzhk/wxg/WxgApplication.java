package com.nzhk.wxg;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"com.nzhk.wxg.**"} )
@MapperScan(value = "com.nzhk.wxg.mapper")
@SpringBootApplication
public class WxgApplication {

    public static void main(String[] args) {
        SpringApplication.run(WxgApplication.class, args);
    }

}
