package com.example.demo.async.spring.servlet.webflux;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@SpringBootApplication
@RestController
@Slf4j
public class MonoEx {
    @GetMapping("/hello")
    Mono<String> hello() {
        log.info("post1");
        Mono<String> m = Mono.just(generateHello()).doOnNext(c -> log.info(c)).log();
        log.info("post2");
        return m;
    }

    @GetMapping("/hello2")
    Mono<String> hello2() {
        log.info("post1");
        Mono<String>m = Mono.fromSupplier(() -> generateHello()).doOnNext(c -> log.info(c)).log();
        log.info("post2");
        return m;
    }

    @GetMapping("/hello3")
    Mono<String> hello3() {
        log.info("post1");
        Mono<String> m = Mono.fromSupplier(() -> generateHello()).doOnNext(c -> log.info(c)).log();
        //m.subscribe();
        String msg = m.block();
        log.info("post2:" + msg);
        // 결과를 block() 한 이후 m 을 리턴하면 스프링 내부에서 Mono 에 해당하는 publisher 를 다시 호출한다.
        // publisher 에서 API 를 호출하거나 DB 접속을 하는 경우 다시 호출됨.
        // return m;
        // 따라서 결과값을 just 로 Mono로 변환한뒤 return 한다. (가능한 소스에서 block() 을 호출하지 말자)
        return Mono.just(msg);
    }

    private String generateHello() {
        log.info("method generateHello()");
        return "Hello Mono";
    }

    public static void main(String[] args) {
        SpringApplication.run(MonoEx.class, args);
    }
}
