package com.xingyang;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.xingyang.mapper")
public class ZmxyMeetApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZmxyMeetApplication.class, args);
    }

}
