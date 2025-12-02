package com.buyticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.buyticket.mapper")
public class BuyTicketApplication {

    public static void main(String[] args) {
        SpringApplication.run(BuyTicketApplication.class, args);
    }
}
