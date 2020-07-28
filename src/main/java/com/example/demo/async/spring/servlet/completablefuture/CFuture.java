package com.example.demo.async.spring.servlet.completablefuture;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CFuture {
    public static void main(String[] args) throws InterruptedException {
        // Async 작업이 끝나고 해당 스레드에서 계속해서 작업을 수행한다.
        CompletableFuture
                .runAsync(() -> log.info("runAsync"))
                .thenRun(() -> log.info("thenRun"))
                .thenRun(() -> log.info("thenRun"));

        log.info("exit");

        // 별도의 pool을 설정하지 않으면 자바7 부터는 ForkJoinPool이 자동으로 사용된다.
        ForkJoinPool.commonPool().shutdown();
        ForkJoinPool.commonPool().awaitTermination(10, TimeUnit.SECONDS);
    }
}
