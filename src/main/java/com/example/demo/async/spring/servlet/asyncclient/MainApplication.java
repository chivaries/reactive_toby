package com.example.demo.async.spring.servlet.asyncclient;

import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

@SpringBootApplication
@Slf4j
public class MainApplication {
    @RestController
    public static class MainController {
//        RestTemplate rt = new RestTemplate();
//
//        @GetMapping("/rest")
//        public String rest(int idx) {
//            // 클라이언트 요청을 처리하면서 외부 서비스로의 Networking I/O 작업을 수행하기 때문에
//            // 외부 서비스로부터의 요청에 대한 응답을 받기 전까지는 blocking 상태가 된다.
//            String res = rt.getForObject("http://localhost:8081/service?req={req}", String.class, "hello" + idx);
//            return res;
//        }

        //asynchronous
        // Tomcat의 스레드가 1개이지만 요청을 비동기적으로 처리함으로써 Tomcat의 스레드는 바로 반환이되어
        // 다시 그 후의 요청에 Tomcat의 스레드를 이용해 요청을 받을 수 있다.
        // 그러나 결과적으로는 실제 비동기 요청을 처리하는 스레드는 요청의 수 만큼 계속 생성한다.
//        AsyncRestTemplate rt = new AsyncRestTemplate();

        // tomcat 스레드 1개, netty가 non blocking I/O를 사용하는데 필요로 하는 몇개의 스레드가 추가된 것 말고는 스레드 수가 크게 증가하지 않은 것을 확인
        AsyncRestTemplate rt = new AsyncRestTemplate(new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));

//        @GetMapping("/rest")
//        public ListenableFuture<ResponseEntity<String>> rest(int idx) {
//
//            return rt.getForEntity("http://localhost:8081/service?req={req}", String.class, "hello" + idx);
//        }

        @GetMapping("/rest")
        public DeferredResult<String> rest(int idx) {
            // 오브젝트를 만들어서 컨트롤러에서 리턴하면 언제가 될지 모르지만 언제인가 DeferredResult에 값을 써주면
            // 그 값을 응답으로 사용
            DeferredResult<String> dr = new DeferredResult<>();

            ListenableFuture<ResponseEntity<String>> f1 = rt.getForEntity("http://localhost:8081/service?req={req}", String.class, "hello" + idx);

            f1.addCallback(s -> {
                dr.setResult(s.getBody() + "/work");
            },  e -> {
                dr.setErrorResult(e.getMessage());
            });

            return dr;
        }

        @GetMapping("/rest2")
        public DeferredResult<String> rest2(int idx) {
            // 오브젝트를 만들어서 컨트롤러에서 리턴하면 언제가 될지 모르지만 언제인가 DeferredResult에 값을 써주면
            // 그 값을 응답으로 사용
            DeferredResult<String> dr = new DeferredResult<>();

            ListenableFuture<ResponseEntity<String>> f1 = rt.getForEntity("http://localhost:8081/service?req={req}", String.class, "hello" + idx);

            // “/service”를 호출한 결과를 이용해 “/service2”를 호출
            f1.addCallback(s -> {
                ListenableFuture<ResponseEntity<String>> f2 = rt.getForEntity("http://localhost:8081/service2?req={req}", String.class, s.getBody());

                f2.addCallback(s2 -> {
                    dr.setResult(s2.getBody());
                }, e -> {
                    dr.setErrorResult(e.getMessage());
                });
            },  e -> {
                dr.setErrorResult(e.getMessage());
            });

            return dr;
        }

        public static void main(String[] args) {
            SpringApplication.run(MainApplication.class, args);
        }
    }
}
