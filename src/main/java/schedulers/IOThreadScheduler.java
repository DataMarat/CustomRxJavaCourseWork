package schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * IOThreadScheduler uses a cached thread pool for IO-bound operations.
 */
public class IOThreadScheduler implements Scheduler {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public void execute(Runnable task) {
        // выполняем задачу в пуле потоков
        executor.submit(task);
    }
}
