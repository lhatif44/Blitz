package com.blitz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BlitzApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlitzApplication.class, args);
    }
}
