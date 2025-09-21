package com.longjunwang.moni;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MoniApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoniApplication.class, args);
    }

}
