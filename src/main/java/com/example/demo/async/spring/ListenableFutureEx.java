package com.example.demo.async.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.Future;

@SpringBootApplication
@EnableAsync
@Slf4j
public class ListenableFutureEx {
    @Service
    public static class MyService {
        @Async
        public ListenableFuture<String> hello() throws InterruptedException {
            log.info("hello()");
            Thread.sleep(1000);
            return new AsyncResult<>("Hello");
        }
    }

    @Autowired
    MyService myService;

    // 모든 빈이 다 준비된 후 실행됨 (현재는 일종의 컨트롤러라고 생각)
    @Bean
    ApplicationRunner run() {
        return args -> {
            log.info("run()");
            ListenableFuture<String> f = myService.hello();
            f.addCallback(s -> log.info(s), e -> log.error(e.getMessage()));
            log.info("exit");

            Thread.sleep(2000);
        };
    }

    public static void main(String[] args) {
        // try with resource 블록을 이용해 빈이 다 준비된 후 종료되도록 설정
        try(ConfigurableApplicationContext c = SpringApplication.run(ListenableFutureEx.class, args)) {
        }
    }
}
