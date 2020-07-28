package com.example.demo.async.spring.servlet.webflux;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@SpringBootApplication
@RestController
@Slf4j
public class FluxEx {
    @GetMapping("/event/{id}")
    Mono<List<Event>> event(@PathVariable long id) {
        List<Event> list = Arrays.asList(new Event(1L, "event1"), new Event(2L, "event2"));
        // list 안에 객체를 가공하려면 list 내에서 작업이 필요
        return Mono.just(list);
    }

    @GetMapping("/events")
    Flux<Event> events() {
        return Flux.just(new Event(1L, "event1"), new Event(2L, "event2"));
    }

    @GetMapping("/events2")
    Flux<Event> events2() {
        List<Event> list = Arrays.asList(new Event(1L, "event1"), new Event(2L, "event2"));
        // 왜 Mono<list> 대신 Flux 를 사용하는가?
        // fromIterable 결과 값을 가지고 바로 스트림 함수 적용가능
        // http stream 을 이용하는 경우 편리
        return Flux.fromIterable(list);
    }

    @GetMapping(value = "/streamEvents", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<Event> streamEvents() {
        List<Event> list = Arrays.asList(new Event(1L, "event1"), new Event(2L, "event2"));
        // http stream 을 이용하는 경우 편리
        return Flux.fromIterable(list);
    }

    @GetMapping(value = "/streamEvents2", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<Event> streamEvents2() {
        return Flux
                .fromStream(Stream.generate(() -> new Event(System.currentTimeMillis(), "value")))
                .delayElements(Duration.ofSeconds(1))
                .take(10);
    }

    @GetMapping(value = "/streamEvents3", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<Event> streamEvents3() {
        return Flux
                .<Event>generate(sink -> sink.next(new Event(System.currentTimeMillis(), "value")))
                .delayElements(Duration.ofSeconds(1))
                .take(10);
    }

    @GetMapping(value = "/streamEvents4", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<Event> streamEvents4() {
        return Flux
                .<Event, Long>generate(() -> 1L, (id, sink) -> {
                    sink.next(new Event(id, "value" + id));
                    return id + 1;
                })
                .delayElements(Duration.ofSeconds(1))
                .take(10);
    }

    @GetMapping(value = "/streamEvents5", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<Event> streamEvents5() {
        Flux<Event> es = Flux.<Event, Long>generate(() -> 1L, (id, sink) -> {
            sink.next(new Event(id, "value" + id));
            return id + 1;
        });

        Flux<Long> interval = Flux.interval(Duration.ofSeconds(1));

        return Flux.zip(es, interval).map(tuple -> tuple.getT1()).take(10);
    }

    @GetMapping(value = "/streamEvents6", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<Event> streamEvents6() {
        Flux<String> es = Flux.generate(sink -> sink.next("Value"));
        Flux<Long> interval = Flux.interval(Duration.ofSeconds(1));

        // zipper 역활
        return Flux.zip(es, interval).map(tuple -> new Event(tuple.getT2() + 1, tuple.getT1())).take(10);
    }


    public static void main(String[] args) {
        SpringApplication.run(FluxEx.class, args);
    }

    @Data @AllArgsConstructor
    public static class Event {
        long id;
        String value;
    }
}
