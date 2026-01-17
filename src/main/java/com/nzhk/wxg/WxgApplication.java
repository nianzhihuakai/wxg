package com.nzhk.wxg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(value = "com.nzhk.wxg.*")
@SpringBootApplication
public class WxgApplication {

    public static void main(String[] args) {
        SpringApplication.run(WxgApplication.class, args);
    }

}
