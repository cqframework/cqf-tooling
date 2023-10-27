package org.opencds.cqf.tooling.common;

import org.opencds.cqf.tooling.utilities.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadUtils {
    /**
     * Executes a list of tasks concurrently using a thread pool.
     * <p>
     * This method takes a list of Callable tasks and executes them concurrently using a thread pool. It utilizes
     * an ExecutorService with a cached thread pool configuration to manage the execution of the tasks.
     * The method waits for all tasks to complete before returning.
     *
     * @param tasks A list of Callable tasks to execute concurrently.
     */
    public static void executeTasks(List<Callable<Void>> tasks) {
        if (tasks == null || tasks.isEmpty()){
            return;
        }else{
            System.out.println("Executing " + tasks.size() + " tasks: \n");
        }

        //let OS handle threading:
        ExecutorService executorService = Executors.newCachedThreadPool();// Submit tasks and obtain futures
        try {
            List<Future<Void>> futures = new ArrayList<>();
            for (Callable<Void> task : tasks) {
                futures.add(executorService.submit(task));
            }

            // Wait for all tasks to complete
            for (Future<Void> future : futures) {
                future.get();
            }
        } catch (Exception e) {
            LogUtils.putException("ThreadUtils.executeTasks", e);
        } finally {
            executorService.shutdown();
        }
    }

    public static void executeTasks(Queue<Callable<Void>> callables) {

        executeTasks(new ArrayList<>(callables));
    }
}
