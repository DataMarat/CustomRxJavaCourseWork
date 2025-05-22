package core;

/**
 * Observer interface defines the core methods to receive events from an Observable stream.
 *
 * @param <T> the type of item the Observer expects to observe
 */
public interface Observer<T> {

    /**
     * Called when a new item is emitted.
     *
     * @param item the item emitted by the Observable
     */
    void onNext(T item);

    /**
     * Called when an error occurs during stream processing.
     *
     * @param throwable the exception thrown
     */
    void onError(Throwable throwable);

    /**
     * Called once the Observable has successfully completed emitting all items.
     */
    void onComplete();
}
