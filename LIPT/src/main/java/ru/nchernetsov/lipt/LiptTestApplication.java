package ru.nchernetsov.lipt;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableProcessApplication
@EnableCaching
public class LiptTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(LiptTestApplication.class, args);
    }
}
