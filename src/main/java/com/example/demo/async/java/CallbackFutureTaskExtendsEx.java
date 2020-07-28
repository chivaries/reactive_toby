package com.example.demo.async.java;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.*;

@Slf4j
public class CallbackFutureTaskExtendsEx {
    interface SuccessCallback {
        void onSuccess(String result);
    }

    public static class CallbackFutureTask extends FutureTask<String> {
        SuccessCallback sc;

        public CallbackFutureTask(Callable<String> callable, SuccessCallback sc) {
            super(callable);
            this.sc = Objects.requireNonNull(sc);
        }

        @Override
        protected void done() {
            super.done();
            try {
                this.sc.onSuccess(get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ExecutorService es = Executors.newCachedThreadPool();

        CallbackFutureTask f = new CallbackFutureTask(() -> {
            Thread.sleep(2000);
            log.info("Async");
            return "Hello";
        }, log::info);

        es.execute(f);
        es.shutdown();

        log.info("EXIT");
    }
}
