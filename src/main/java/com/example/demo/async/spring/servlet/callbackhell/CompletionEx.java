package com.example.demo.async.spring.servlet.callbackhell;

import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.function.Consumer;
import java.util.function.Function;


@SpringBootApplication
@EnableAsync
@Slf4j
public class CompletionEx {
    @RestController
    public static class MyController {
        AsyncRestTemplate rt = new AsyncRestTemplate(new Netty4ClientHttpRequestFactory(new NioEventLoopGroup(1)));

        @Autowired
        MyService myService;

        static final String URL1 = "http://localhost:8081/service?req={req}";
        static final String URL2 = "http://localhost:8081/service2?req={req}";

        @GetMapping("/rest")
        public DeferredResult<String> rest(int idx) {
            DeferredResult<String> dr = new DeferredResult<>();

            Completion
                    .from(rt.getForEntity(URL1, String.class, "hello" + idx))
                    .andApply(r -> rt.getForEntity(URL2, String.class, r.getBody()))
                    .andError(e -> dr.setErrorResult(e))
                    .andAccept(s -> dr.setResult(s.getBody()));

           /*
            ListenableFuture<ResponseEntity<String>> f1 = rt.getForEntity("http://localhost:8081/service?req={req}", String.class, "hello" + idx);
            f1.addCallback(s -> {
                ListenableFuture<ResponseEntity<String>> f2 = rt.getForEntity("http://localhost:8081/service2?req={req}", String.class, s.getBody());
                f2.addCallback(s2 -> {
                    ListenableFuture<String> f3 = myService.work(s2.getBody());
                    f3.addCallback(s3 -> {
                        dr.setResult(s3);
                    }, e -> {
                        dr.setErrorResult(e.getMessage());
                    });
                }, e -> {
                    dr.setErrorResult(e.getMessage());
                });
            }, e -> {
                dr.setErrorResult(e.getMessage());
            });
            */

            return dr;
        }

    }

    public static class AcceptCompletion extends Completion {
        Consumer<ResponseEntity<String>> con;

        public AcceptCompletion(Consumer<ResponseEntity<String>> con) {
            this.con = con;
        }

        @Override
        public void run(ResponseEntity<String> value) {
            con.accept(value);
        }
    }

    public static class AsyncCompletion extends Completion {
        Function<ResponseEntity<String>, ListenableFuture<ResponseEntity<String>>> fn;

        public AsyncCompletion(Function<ResponseEntity<String>, ListenableFuture<ResponseEntity<String>>> fn) {
            this.fn = fn;
        }

        @Override
        public void run(ResponseEntity<String> value) {
            ListenableFuture<ResponseEntity<String>> lf = fn.apply(value);
            lf.addCallback(s -> this.complete(s), e -> this.error(e));
        }
    }

    public static class ErrorCompletion extends Completion {
        Consumer<Throwable> econ;

        public ErrorCompletion(Consumer<Throwable> econ) {
            this.econ = econ;
        }

        @Override
        public void run(ResponseEntity<String> value) {
            if (next != null) {
                next.run(value);
            }
        }

        @Override
        public void error(Throwable e) {
            econ.accept(e);
        }
    }

    public static class Completion {
        Completion next;

        public static Completion from(ListenableFuture<ResponseEntity<String>> lf) {
            Completion c = new Completion();

            lf.addCallback(s -> {
                c.complete(s);
            }, e -> {
                c.error(e);
            });

            return c;
        }

        public void andAccept(Consumer<ResponseEntity<String>> con) {
            Completion c = new AcceptCompletion(con);
            this.next = c;
        }

        public Completion andApply(Function<ResponseEntity<String>, ListenableFuture<ResponseEntity<String>>> fn) {
            Completion c = new AsyncCompletion(fn);
            this.next = c;
            return c;
        }

        public Completion andError(Consumer<Throwable> econ) {
            Completion c = new ErrorCompletion(econ);
            this.next = c;
            return c;
        }

        public void complete(ResponseEntity<String> s) {
            if (next != null) next.run(s);
        }

        public void run(ResponseEntity<String> value) {
        }

        public void error(Throwable e) {
        }
    }

    @Service
    public static class MyService {
        @Async
        public ListenableFuture<String> work(String req) {
            return new AsyncResult<>(req + "/asyncwork");
        }
    }

    @Bean
    public ThreadPoolTaskExecutor myThreadPool() {
        ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
        te.setCorePoolSize(1);
        te.setMaxPoolSize(1);
        te.initialize();
        return te;
    }

    public static void main(String[] args) {
        SpringApplication.run(CompletionEx.class, args);
    }
}
