package core;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import schedulers.Scheduler;

/**
 * Observable represents a stream of data/events that can be observed.
 *
 * @param <T> the type of item the Observable emits
 */
public class Observable<T> {

    private final Consumer<Observer<T>> onSubscribe;

    private Observable(Consumer<Observer<T>> onSubscribe) {
        this.onSubscribe = onSubscribe;
    }

    /**
     * Subscribes the given observer to this Observable stream.
     *
     * @param observer the observer that will receive emissions and notifications
     */
    public void subscribe(Observer<T> observer) {
        try {
            onSubscribe.accept(observer);
        } catch (Throwable t) {
            observer.onError(t);
        }
    }

    /**
     * Creates a new Observable instance from the provided subscription behavior.
     *
     * @param <T> the type of item the Observable emits
     * @param onSubscribe the logic to execute when an Observer subscribes
     * @return a new Observable instance
     */
    public static <T> Observable<T> create(Consumer<Observer<T>> onSubscribe) {
        return new Observable<>(onSubscribe);
    }

    /**
     * Transforms the items emitted by this Observable by applying a function to each item.
     *
     * @param <R> the result type after applying the transformation
     * @param mapper a function to apply to each item emitted by the source Observable
     * @return an Observable that emits the transformed items
     */
    public <R> Observable<R> map(Function<T, R> mapper) {
        return new Observable<>(observer ->
                this.subscribe(new Observer<>() {
                    @Override
                    public void onNext(T item) {
                        try {
                            // применяем трансформацию и передаем результат дальше
                            R mappedItem = mapper.apply(item);
                            observer.onNext(mappedItem);
                        } catch (Throwable t) {
                            observer.onError(t);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        observer.onError(throwable);
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }
                })
        );
    }

    /**
     * Emits only those items from the source Observable that pass the given predicate test.
     *
     * @param predicate a function that evaluates each item to determine if it should be emitted
     * @return an Observable that emits only items that pass the predicate test
     */
    public Observable<T> filter(Predicate<T> predicate) {
        return new Observable<>(observer ->
                this.subscribe(new Observer<>() {
                    @Override
                    public void onNext(T item) {
                        try {
                            // фильтруем элементы по условию
                            if (predicate.test(item)) {
                                observer.onNext(item);
                            }
                        } catch (Throwable t) {
                            observer.onError(t);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        observer.onError(throwable);
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }
                })
        );
    }

    /**
     * Specifies the Scheduler on which the Observable should perform the subscription logic.
     *
     * @param scheduler the Scheduler to perform subscription work on
     * @return a new Observable with subscription logic executed on the given Scheduler
     */
    public Observable<T> subscribeOn(Scheduler scheduler) {
        return new Observable<>(observer ->
                scheduler.execute(() -> Observable.this.subscribe(observer)) // подписка запускается в другом потоке
        );
    }

    /**
     * Specifies the Scheduler on which the Observer should receive emitted items.
     *
     * @param scheduler the Scheduler to deliver items to the observer
     * @return a new Observable that pushes emissions on the specified Scheduler
     */
    public Observable<T> observeOn(Scheduler scheduler) {
        return new Observable<>(observer ->
                this.subscribe(new Observer<>() {
                    @Override
                    public void onNext(T item) {
                        // элементы будут обрабатываться в другом потоке
                        scheduler.execute(() -> observer.onNext(item));
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        scheduler.execute(() -> observer.onError(throwable));
                    }

                    @Override
                    public void onComplete() {
                        scheduler.execute(observer::onComplete);
                    }
                })
        );
    }

    /**
     * Transforms items into Observables and flattens them into a single stream.
     *
     * @param <R> the type of item emitted by the new inner Observables
     * @param mapper function mapping each item into an Observable
     * @return an Observable emitting all items from the mapped Observables
     */
    public <R> Observable<R> flatMap(Function<T, Observable<R>> mapper) {
        return new Observable<>(observer ->
                this.subscribe(new Observer<>() {
                    @Override
                    public void onNext(T item) {
                        try {
                            // создаем внутренний Observable и подписываемся на него
                            Observable<R> inner = mapper.apply(item);
                            inner.subscribe(new Observer<R>() {
                                @Override
                                public void onNext(R innerItem) {
                                    observer.onNext(innerItem);
                                }

                                @Override
                                public void onError(Throwable throwable) {
                                    observer.onError(throwable);
                                }

                                @Override
                                public void onComplete() {
                                    // ничего не делаем — внешний onComplete будет вызван отдельно
                                }
                            });
                        } catch (Throwable t) {
                            observer.onError(t);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        observer.onError(throwable);
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }
                })
        );
    }
}
