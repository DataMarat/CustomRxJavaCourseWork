package test;

import core.DisposableObserver;
import core.Observable;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class OperatorTest {

    @Test
    void testFlatMapCombinesStreams() throws InterruptedException {
        StringBuilder log = new StringBuilder();
        CountDownLatch latch = new CountDownLatch(1);

        Observable<Integer> source = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onComplete();
        });

        source
                .flatMap(i -> Observable.<Integer>create(em -> {
                    em.onNext(i * 10);
                    em.onNext(i * 10 + 1);
                    em.onComplete();
                }))
                .subscribe(new core.Observer<Integer>() {
                    @Override
                    public void onNext(Integer item) {
                        log.append(item).append(" ");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        fail();
                    }

                    @Override
                    public void onComplete() {
                        latch.countDown();
                    }
                });

        latch.await();
        assertEquals("10 11 20 21 ", log.toString());
    }

    @Test
    void testDisposableStopsEmission() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(1);

        Observable<Integer> observable = Observable.create(emitter -> {
            for (int i = 0; i < 100; i++) {
                emitter.onNext(i);
            }
            emitter.onComplete();
        });

        DisposableObserver<Integer> observer = new DisposableObserver<Integer>() {
            @Override
            public void onNext(Integer item) {
                if (isActive()) {
                    counter.incrementAndGet(); // увеличиваем счётчик
                }
                if (item == 3) {
                    dispose(); // отменяем подписку
                }
            }

            @Override
            public void onError(Throwable throwable) {}

            @Override
            public void onComplete() {
                latch.countDown();
            }
        };

        observable.subscribe(observer);
        latch.await();

        assertEquals(4, counter.get()); // только элементы 0,1,2,3
    }
}
