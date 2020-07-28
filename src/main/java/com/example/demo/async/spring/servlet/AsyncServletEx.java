package com.example.demo.async.spring.servlet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Callable;

@SpringBootApplication
@EnableAsync
@Slf4j
public class AsyncServletEx {
    @RestController
    public static class MyController {
        @GetMapping("/callable")
        public Callable<String> callable() {
            log.info("callable");
            return () -> {
                log.info("async");
                Thread.sleep(2000);
                return "hello";
            };
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(AsyncServletEx.class, args);
    }
}
