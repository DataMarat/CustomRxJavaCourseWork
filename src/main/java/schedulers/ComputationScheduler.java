package schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ComputationScheduler uses a fixed-size thread pool suitable for CPU-bound tasks.
 */
public class ComputationScheduler implements Scheduler {

    private final ExecutorService executor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()
    );

    @Override
    public void execute(Runnable task) {
        // выполняем задачу в ограниченном числе потоков
        executor.submit(task);
    }
}
