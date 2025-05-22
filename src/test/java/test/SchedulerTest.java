package test;

import core.Observable;
import core.Observer;
import org.junit.jupiter.api.Test;
import schedulers.ComputationScheduler;
import schedulers.IOThreadScheduler;
import schedulers.SingleThreadScheduler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class SchedulerTest {

    @Test
    void testSubscribeOnRunsInDifferentThread() throws InterruptedException {
        AtomicReference<String> threadName = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Observable<String> observable = Observable.create(emitter -> {
            threadName.set(Thread.currentThread().getName());
            emitter.onComplete();
            latch.countDown();
        });

        observable
                .subscribeOn(new IOThreadScheduler())
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(String item) {}

                    @Override
                    public void onError(Throwable throwable) {
                        fail();
                    }

                    @Override
                    public void onComplete() {}
                });

        latch.await();
        assertTrue(threadName.get().toLowerCase().contains("pool") || threadName.get().toLowerCase().contains("thread"));
    }

    @Test
    void testObserveOnRunsInDifferentThread() throws InterruptedException {
        AtomicReference<String> threadName = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Observable<String> observable = Observable.create(emitter -> {
            emitter.onNext("X");
            emitter.onComplete();
        });

        observable
                .observeOn(new ComputationScheduler())
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(String item) {
                        threadName.set(Thread.currentThread().getName());
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
        assertTrue(threadName.get().toLowerCase().contains("pool") || threadName.get().toLowerCase().contains("thread"));
    }

    @Test
    void testSingleThreadSchedulerExecutesInSameThread() throws InterruptedException {
        SingleThreadScheduler scheduler = new SingleThreadScheduler();
        AtomicReference<String> thread1 = new AtomicReference<>();
        AtomicReference<String> thread2 = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(2);

        scheduler.execute(() -> {
            thread1.set(Thread.currentThread().getName());
            latch.countDown();
        });

        scheduler.execute(() -> {
            thread2.set(Thread.currentThread().getName());
            latch.countDown();
        });

        latch.await();
        assertEquals(thread1.get(), thread2.get());
    }
}
