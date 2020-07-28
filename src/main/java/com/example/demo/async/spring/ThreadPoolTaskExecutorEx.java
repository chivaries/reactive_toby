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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.ThreadPoolExecutor;

@SpringBootApplication
@EnableAsync
@Slf4j
public class ThreadPoolTaskExecutorEx {
    @Service
    public static class MyService {
        /*
            기본적으로 SimpleAsyncTaskExecutor를 사용한다. 스레드를 계속 새로 만들어 사용하기 때문에 비효율적이다.
        */
        @Async
        public ListenableFuture<String> hello() throws InterruptedException {
            log.info("hello()");
            Thread.sleep(1000);
            return new AsyncResult<>("Hello");
        }
    }

    @Bean
    ThreadPoolTaskExecutor tp() {
        ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
        // 1) 스레드 풀을 해당 개수까지 기본적으로 생성함. 처음 요청이 들어올 때 poll size만큼 생성한다.
        te.setCorePoolSize(10);
        // 2) 지금 당장은 Core 스레드를 모두 사용중일때, 큐에 만들어 대기시킨다.
        te.setQueueCapacity(50);
        // 3) 대기하는 작업이 큐에 꽉 찰 경우, 풀을 해당 개수까지 더 생성한다.
        te.setMaxPoolSize(100);
        te.setThreadNamePrefix("myThread");
        return te;
    }

    @Autowired
    ListenableFutureEx.MyService myService;

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
