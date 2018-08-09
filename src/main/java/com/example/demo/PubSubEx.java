package com.example.demo;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.*;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

public class PubSubEx {
    public static void main(String[] args) throws InterruptedException {
        // Publisher <- Observable
        // Subscriber <- Observer

        // Publisher 가 Subscriber 에게 반드시 호출해야 할 method
        // onSubscribe onNext* (onError | onComplete)? -> reactive protocol 의 핵심

        // Subscription 을 통해서 subscriber 는 publisher 에게 속도 조절 요청 가능 -> back pressure
        // publisher 가 너무 많은 량을 한꺼번에 보내고 subscriber 가 처리를 못하는 경우
        // publish 된 데이터는 어딘가에 있어야함 -> buffer -> memory 량이 폭발적으로 증가
        // back pressure 조절을 통해 속도를 맞춘다.

        Iterable<Integer> itr = Arrays.asList(1,2,3,4,5);
        ExecutorService es = Executors.newSingleThreadExecutor();

        Publisher p = new Publisher() {
            @Override
            public void subscribe(Subscriber subscriber) {
                Iterator<Integer> it = itr.iterator();

                subscriber.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) {
                        es.execute(() -> {
                            int i = 0;
                            try {
                                while (i++ < n) {
                                    if (it.hasNext()) {
                                        subscriber.onNext(it.next());
                                    } else {
                                        subscriber.onComplete();
                                        break;
                                    }
                                }
                            } catch (RuntimeException re) {
                                subscriber.onError(re);
                            }
                        });
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };

        Subscriber<Integer> s = new Subscriber<>() {
            Subscription subscription;

            @Override
            public void onSubscribe(Subscription subscription) {
                System.out.println(Thread.currentThread().getName() + " onSubscribe");

                this.subscription = subscription;
                // 전부 다 내놔!
                // subscription.request(Long.MAX_VALUE);

                this.subscription.request(1);
            }

            @Override
            public void onNext(Integer item) {
                System.out.println(Thread.currentThread().getName() + " onNext " + item);
                this.subscription.request(1);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("onError");
            }

            @Override
            public void onComplete() {
                System.out.println(Thread.currentThread().getName() + " onComplete");
            }
        };

        p.subscribe(s);

        es.awaitTermination(10, TimeUnit.HOURS);
        es.shutdown();
    }
}
