package com.example.demo.async.spring.servlet.webflux;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@SpringBootApplication
@RestController
@Slf4j
@EnableAsync
public class WebfluxEx {
    // com.example.demo.async.spring.servlet.asyncclient.RemoteApplication 사용
    static final String URL1 = "http://localhost:8081/service?req={req}";
    static final String URL2 = "http://localhost:8081/service2?req={req}";

    @Autowired
    MyService myService;

    WebClient client = WebClient.create();

//    @Bean
//    NettyReactiveWebServerFactory nettyReactiveWebServerFactory() {
//        return new NettyReactiveWebServerFactory();
//    }

    @GetMapping("/rest")
    public Mono<String> rest(int idx) {
        // WebClient 가 Mono 를 리턴하므로 subscribe 를 해야 실행된다.
        // Spring 이 대신 subscribe 한다. -> Mono<String> 을 리턴하면 Spring 이 알아서 subscribe
        Mono<ClientResponse> res = client.get().uri(URL1, idx).exchange();

        // Mono 에서 데이터를 꺼내는 방법은 Stream 과 동일
        //Mono<Mono<String> body = res.map(clientResponse -> clientResponse.bodyToMono(String.class));
        Mono<String> body = res.flatMap(clientResponse -> clientResponse.bodyToMono(String.class));
        return body;

        // 한라인으로 표시
        //return client.get().uri(URL1, idx).exchange().flatMap(clientResponse -> clientResponse.bodyToMono(String.class));
    }

    @GetMapping("/rest2")
    public Mono<String> rest2(int idx) {
        return client.get().uri(URL1, idx).exchange() // Mono<ClientResponse>
                .flatMap(clientResponse -> clientResponse.bodyToMono(String.class)) // Mono<String>
                .doOnNext(c -> log.info(c.toString())) // reactive-http-nio-1 thread 에서 실행됨
                .flatMap(res1 -> client.get().uri(URL2, res1).exchange()) // Mono<ClientResponse>
                .flatMap(c -> c.bodyToMono(String.class)) // Mono<String>
                .doOnNext(c -> log.info(c.toString())) //reactive-http-nio-1 thread 에서 실행됨
                /*
                    아래 service 호출도 reactive-http-nio-1 thread 에서 같이 호출
                    문제점 -> service 호출이 시간이 오래걸릴 경우 reactive-http-nio-1 thread 가 blocking 되는 효과 발생
                    해결 -> 별도 thread 로 호출해야 한다!!
                 */
                .map(res2 -> myService.work(res2)) // Mono<String>
                .doOnNext(c -> log.info(c.toString())); //reactive-http-nio-1 thread 에서 실행됨
    }

    @GetMapping("/rest3")
    public Mono<String> rest3(int idx) {
        return client.get().uri(URL1, idx).exchange() // Mono<ClientResponse>
                .flatMap(clientResponse -> clientResponse.bodyToMono(String.class)) // Mono<String>
                .doOnNext(c -> log.info(c.toString())) // reactive-http-nio-1 thread 에서 실행됨
                .flatMap(res1 -> client.get().uri(URL2, res1).exchange()) // Mono<ClientResponse>
                .flatMap(c -> c.bodyToMono(String.class)) // Mono<String>
                .doOnNext(c -> log.info(c.toString())) //reactive-http-nio-1 thread 에서 실행됨
                .flatMap(res2 -> Mono.fromCompletionStage(myService.asyncWork(res2))) // CompletableFuture<String> -> Mono<String>
                .doOnNext(c -> log.info(c.toString())); // 별도 thread 에서 비동기로 처리 -> reactive-http-nio-1 thread 가 blocking 되지 않음
    }

    @Service
    public static class MyService {
        public String work(String req) {
            return req + "/work";
        }

        @Async
        public CompletableFuture<String> asyncWork(String req) {
            return CompletableFuture.completedFuture(req + "/asyncwork");
        }
    }

    public static void main(String[] args) {
        System.setProperty("reactor.ipc.netty.workerCount", "1");
        System.setProperty("reactor.ipc.netty.pool.maxConnection", "2000");
        SpringApplication.run(WebfluxEx.class, args);
    }
}