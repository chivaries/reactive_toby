package com.example.demo.observable;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ObservableEx {
    // Iterable <------> Observable (duality : 쌍대성)
    // Pull              Push
    /*
    public static void main(String[] args) {
        Iterable<Integer> iter = () ->
                new Iterator<Integer>() {
                    int i = 0;
                    final static int MAX = 10;

                    @Override
                    public boolean hasNext() {
                        return i < MAX;
                    }

                    @Override
                    public Integer next() {
                        return ++i;
                    }
                };

        for(Integer i : iter) {
            System.out.println(i);
        }

        for(Iterator<Integer> it = iter.iterator(); it.hasNext(); ) {
            System.out.println(it.next());
        }
    }
    */

    // Observable/Publisher (Event Source) -> Event/Data -> Observer
    static class IntObservable extends Observable implements Runnable {
        @Override
        public void run() {
            for(int i=1; i<=10; i++) {
                setChanged();
                // observer / subscriber 의 update() 호출
                notifyObservers(i); // push
                // int i = it.next() // pull
            }
        }
    }

    public static void main(String[] args) {
        Observer ob = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                System.out.println(Thread.currentThread().getName() + " " + arg);
            }
        };

        IntObservable io = new IntObservable();
        io.addObserver(ob);

        // main thread 에서 실행
        //io.run();

        // 별도 thread 에서 실행
        ExecutorService es = Executors.newSingleThreadExecutor();
        es.execute(io);

        System.out.println(Thread.currentThread().getName() + " EXIT");
        es.shutdown();
    }

    /*
        Observable 의 한계
        1. Complete 개념이 없다.
        2. Error 처리 ??

        -> 확장된 observer 패턴을 만듦
     */
}
