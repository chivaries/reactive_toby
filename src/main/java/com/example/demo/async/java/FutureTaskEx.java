package com.example.demo.async.java;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

@Slf4j
public class FutureTaskEx {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService es = Executors.newCachedThreadPool();

        // FutureTask는 비동기 작업 생성과 실행을 분리하여 진행
        FutureTask<String> f = new FutureTask<>(() -> {
            Thread.sleep(2000);
            log.info("Async");
            return "Hello";
        });

        es.execute(f);

        log.info(String.valueOf(f.isDone()));
        Thread.sleep(2000);
        log.info("Exit");
        log.info(String.valueOf(f.isDone()));
        log.info(f.get());
    }
}
