package org.opencds.cqf.tooling.common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadUtils {

    public static boolean executeTasks(List<Callable<Void>> tasks) {
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
            e.printStackTrace();
            return false;
        } finally {
            executorService.shutdown();
        }

        return true;
    }

}
