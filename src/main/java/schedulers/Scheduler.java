package schedulers;

/**
 * Scheduler is responsible for scheduling tasks on different threads or thread pools.
 */
public interface Scheduler {

    /**
     * Executes a given task on the Scheduler's associated thread or thread pool.
     *
     * @param task the Runnable task to execute
     */
    void execute(Runnable task);
}
