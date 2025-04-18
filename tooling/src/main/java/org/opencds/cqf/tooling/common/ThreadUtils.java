package org.opencds.cqf.tooling.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadUtils {
    protected static final Logger logger = LoggerFactory.getLogger(ThreadUtils.class);

    private static List<ExecutorService> runningExecutors = new ArrayList<>();

    /**
     * Executes a list of tasks concurrently using a thread pool.
     * <p>
     * This method takes a list of Callable tasks and executes them concurrently using a thread pool. It utilizes
     * an ExecutorService with a cached thread pool configuration to manage the execution of the tasks.
     * The method waits for all tasks to complete before returning.
     *
     * @param tasks A list of Callable tasks to execute concurrently.
     */
    public static void executeTasks(List<Callable<Void>> tasks, ExecutorService executor) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }

        runningExecutors.add(executor);

        List<Callable<Void>> retryTasks = new ArrayList<>();

        //let OS handle threading:
        try {
            List<Future<Void>> futures = new ArrayList<>();
            for (Callable<Void> task : tasks) {
                try {
                    futures.add(executor.submit(task));
                } catch (OutOfMemoryError e) {
                    retryTasks.add(task);
                }
            }

            // Wait for all tasks to complete
            for (Future<Void> future : futures) {
                future.get();
            }
        } catch (Exception e) {
            logger.error("ThreadUtils.executeTasks: ", e);
        } finally {
            if (retryTasks.isEmpty()) {
                runningExecutors.remove(executor);
                executor.shutdown();
            }else{
                executeTasks(retryTasks, executor);
            }
        }
    }

    public static void executeTasks(List<Callable<Void>> tasks) {
        executeTasks(tasks, Executors.newCachedThreadPool());
    }

    public static void executeTasks(Queue<Callable<Void>> callables) {
        executeTasks(new ArrayList<>(callables), Executors.newCachedThreadPool());
    }

    public static void shutdownRunningExecutors() {
        try {
            if (runningExecutors.isEmpty()) return;
            for (ExecutorService es : runningExecutors) {
                es.shutdownNow();
            }
            runningExecutors = new ArrayList<>();
        }catch (Exception e){
            //fail silently, shutting down anyways
        }
    }
}
