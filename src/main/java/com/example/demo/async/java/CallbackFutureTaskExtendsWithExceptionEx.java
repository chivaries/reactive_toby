package com.example.demo.async.java;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.*;

@Slf4j
public class CallbackFutureTaskExtendsWithExceptionEx {
    interface SuccessCallback {
        void onSuccess(String result);
    }

    interface ExceptionCallback {
        void onError(Throwable t);
    }

    public static class CallbackFutureTask extends FutureTask<String> {
        SuccessCallback sc;
        ExceptionCallback ec;

        public CallbackFutureTask(Callable<String> callable, SuccessCallback sc, ExceptionCallback ec) {
            super(callable);
            this.sc = Objects.requireNonNull(sc);
            this.ec = Objects.requireNonNull(ec);
        }

        @Override
        protected void done() {
            super.done();
            try {
                this.sc.onSuccess(get());
            /*
             InterruptedException은 예외긴 예외이지만, 현재 작업을 수행하지 말고 중단해라 라고 메시지를 보내는 용도이다.
             따라서 현재 스레드에 interrupt를 체크하고 종료한다.
             */
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                // 래핑된 에러를 빼내서 전달
                ec.onError(e.getCause());
            }
        }
    }

    public static void main(String[] args) {
        ExecutorService es = Executors.newCachedThreadPool();

        CallbackFutureTask f = new CallbackFutureTask(() -> {
            Thread.sleep(2000);
            log.info("Async");
            return "Hello";
        },
                s -> log.info("Result: {}", s),
                e -> log.info("Error: {}", e.getMessage())
        );

        es.execute(f);
        es.shutdown();

        log.info("EXIT");
    }
}
