package core;

/**
 * Disposable represents a handle to cancel an active subscription.
 */
public interface Disposable {

    /**
     * Cancels the subscription.
     */
    void dispose();

    /**
     * Checks if the subscription has already been disposed.
     *
     * @return true if disposed, false otherwise
     */
    boolean isDisposed();
}
