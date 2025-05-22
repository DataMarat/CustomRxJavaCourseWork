package core;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * DisposableObserver is an Observer with built-in disposal logic.
 *
 * @param <T> the type of item observed
 */
public abstract class DisposableObserver<T> implements Observer<T>, Disposable {

    private final AtomicBoolean disposed = new AtomicBoolean(false);

    @Override
    public void dispose() {
        disposed.set(true);
    }

    @Override
    public boolean isDisposed() {
        return disposed.get();
    }

    /**
     * Utility method for subclasses to check before delivering events.
     *
     * @return true if not disposed
     */
    protected boolean isActive() {
        return !disposed.get();
    }
}
