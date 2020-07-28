package com.example.demo.async.spring.servlet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableAsync
@Slf4j
public class ServletEx {
    @RestController
    public static class MyController {
        @GetMapping("/sync")
        public String sync() throws InterruptedException {
            log.info("sync");
            Thread.sleep(2000);
            return "hello";
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(ServletEx.class, args);
    }
}

