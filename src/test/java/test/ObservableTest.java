package test;

import core.Observable;
import core.Observer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObservableTest {

    @Test
    void testBasicObservableEmitsItemsAndCompletes() {
        StringBuilder log = new StringBuilder();

        Observable<String> observable = Observable.create(emitter -> {
            emitter.onNext("Hello");
            emitter.onNext("World");
            emitter.onComplete();
        });

        observable.subscribe(new Observer<>() {
            @Override
            public void onNext(String item) {
                log.append(item).append(" ");
            }

            @Override
            public void onError(Throwable throwable) {
                fail("Should not reach onError");
            }

            @Override
            public void onComplete() {
                log.append("Done");
            }
        });

        assertEquals("Hello World Done", log.toString().trim());
    }

    @Test
    void testMapOperator() {
        StringBuilder log = new StringBuilder();

        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onComplete();
        });

        observable
                .map(i -> "Mapped:" + i)
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(String item) {
                        log.append(item).append(";");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        fail("Unexpected error");
                    }

                    @Override
                    public void onComplete() {
                        log.append("C");
                    }
                });

        assertEquals("Mapped:1;Mapped:2;C", log.toString());
    }

    @Test
    void testFilterOperator() {
        StringBuilder log = new StringBuilder();

        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.onComplete();
        });

        observable
                .filter(i -> i % 2 == 1)
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(Integer item) {
                        log.append(item);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        fail("Should not trigger error");
                    }

                    @Override
                    public void onComplete() {
                        log.append("X");
                    }
                });

        assertEquals("13X", log.toString());
    }
}
