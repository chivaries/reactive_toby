package com.example.demo.async.spring.servlet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class LoadTestSimultaneously {
    static AtomicInteger counter = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
        ExecutorService es = Executors.newFixedThreadPool(100);

        RestTemplate rt = new RestTemplate();
        String url = "http://localhost:8080/rest3?idx={idx}";

        CyclicBarrier barrier = new CyclicBarrier(101);

        for(int i = 0; i < 100; i++) {
            // submit이 받는 callable은 return을 가질 수 있으며, exception도 던질 수 있다.
            es.submit(() -> {
                int idx = counter.addAndGet(1);
                log.info("Thread {}", idx);
                barrier.await();

                StopWatch sw = new StopWatch();
                sw.start();

                String res = rt.getForObject(url, String.class, idx);

                sw.stop();
                log.info("idx: {}, Elapsed: {} -> res: {}", idx, sw.getTotalTimeSeconds(), res);

                // functional interface 가 Callable 임을 알려주는 의미없는 return
                return null;
            });
        }

        // await을 만난 스레드가 101번째가 될 때, 모든 스레드들도 await에서 풀려나 이후 로직을 수행한다.
        // 메인 스레드 1개, Executors.newFixedThreadPool로 생성한 스레드 100개
        barrier.await();
        StopWatch main = new StopWatch();
        main.start();

        es.shutdown();

        // 지정된 시간이 타임아웃 걸리기 전이라면 대기작업이 진행될 때까지 기다린다.
        // (100초안에 작업이 끝날때까지 기다리거나, 100초가 초과되면 종료)
        es.awaitTermination(100, TimeUnit.SECONDS);
        main.stop();
        log.info("Total: {}", main.getTotalTimeSeconds());
    }
}
