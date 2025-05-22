package schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SingleThreadScheduler executes all tasks sequentially on a single thread.
 */
public class SingleThreadScheduler implements Scheduler {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void execute(Runnable task) {
        // все задачи выполняются последовательно
        executor.submit(task);
    }
}
