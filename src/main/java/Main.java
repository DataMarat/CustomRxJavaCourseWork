import core.Observable;
import core.Observer;
import core.DisposableObserver;
import core.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schedulers.ComputationScheduler;
import schedulers.IOThreadScheduler;
import schedulers.SingleThreadScheduler;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        IOThreadScheduler ioScheduler = new IOThreadScheduler();
        ComputationScheduler computationScheduler = new ComputationScheduler();
        SingleThreadScheduler singleThreadScheduler = new SingleThreadScheduler();

        // === Example 1: flatMap ===
        logger.info("Example 1: flatMap");

        Observable<Integer> source = Observable.create(observer -> {
            for (int i = 1; i <= 3; i++) {
                observer.onNext(i);
            }
            observer.onComplete();
        });

        source
                .flatMap(i -> Observable.<String>create(inner -> {
                    inner.onNext("Item: " + (i * 10));
                    inner.onNext("Item: " + (i * 20));
                    inner.onComplete();
                }))
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(String item) {
                        logger.info("[flatMap] {}", item);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        logger.error("[flatMap] Error: {}", throwable.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        logger.info("[flatMap] Completed");
                    }
                });

        // === Example 2: Error handling ===
        logger.info("Example 2: Error handling");

        Observable.create(observer -> {
            observer.onNext("A");
            observer.onNext("B");
            throw new RuntimeException("Simulated error");
        }).subscribe(new Observer<>() {
            @Override
            public void onNext(Object item) {
                logger.info("[error] Received: {}", item);
            }

            @Override
            public void onError(Throwable throwable) {
                logger.warn("[error] Handled: {}", throwable.getMessage());
            }

            @Override
            public void onComplete() {
                logger.info("[error] This won't be printed");
            }
        });

        // === Example 3: Disposable ===
        logger.info("Example 3: Disposable");

        Observable<Integer> infinite = Observable.create(observer -> {
            int i = 0;
            while (true) {
                if (observer instanceof Disposable && ((Disposable) observer).isDisposed()) {
                    break; // остановка генерации, если подписка отменена
                }
                observer.onNext(i++);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    observer.onError(e);
                    break;
                }
            }
        });

        DisposableObserver<Integer> disposableObserver = new DisposableObserver<>() {
            @Override
            public void onNext(Integer item) {
                logger.info("[infinite] {}", item);
                if (item >= 5) {
                    dispose();
                    logger.info("[infinite] Disposed!");
                }
            }

            @Override
            public void onError(Throwable throwable) {
                logger.error("[infinite] Error: {}", throwable.getMessage());
            }

            @Override
            public void onComplete() {
                logger.info("[infinite] Completed");
            }
        };

        infinite
                .subscribeOn(ioScheduler)
                .observeOn(singleThreadScheduler)
                .subscribe(disposableObserver);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            logger.error("Main thread interrupted: {}", e.getMessage());
        }

        logger.info("Done.");
    }
}
