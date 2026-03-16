package org.opencds.cqf.tooling.common;

import static org.testng.Assert.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class ThreadUtilsTest {

    @AfterMethod
    public void cleanup() {
        ThreadUtils.shutdownRunningExecutors();
    }

    @Test
    public void testExecuteTasksRunsAllTasks() {
        AtomicInteger counter = new AtomicInteger(0);
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            tasks.add(() -> {
                counter.incrementAndGet();
                return null;
            });
        }

        ThreadUtils.executeTasks(tasks);
        assertEquals(counter.get(), 5, "All 5 tasks should have executed");
    }

    @Test
    public void testExecuteTasksWithExecutor() {
        AtomicInteger counter = new AtomicInteger(0);
        List<Callable<Void>> tasks = List.of(() -> {
            counter.incrementAndGet();
            return null;
        });

        ThreadUtils.executeTasks(tasks, Executors.newSingleThreadExecutor());
        assertEquals(counter.get(), 1);
    }

    @Test
    public void testExecuteTasksNullList() {
        // Should be a no-op
        ThreadUtils.executeTasks((List<Callable<Void>>) null);
    }

    @Test
    public void testExecuteTasksEmptyList() {
        // Should be a no-op
        ThreadUtils.executeTasks(new ArrayList<>());
    }

    @Test
    public void testExecuteTasksFromQueue() {
        AtomicInteger counter = new AtomicInteger(0);
        Queue<Callable<Void>> queue = new LinkedList<>();
        queue.add(() -> {
            counter.incrementAndGet();
            return null;
        });
        queue.add(() -> {
            counter.incrementAndGet();
            return null;
        });

        ThreadUtils.executeTasks(queue);
        assertEquals(counter.get(), 2);
    }

    @Test
    public void testExecuteTasksHandlesExceptionInTask() {
        List<Callable<Void>> tasks = List.of(() -> {
            throw new RuntimeException("task failed");
        });

        // Should not throw — exception is caught and logged
        ThreadUtils.executeTasks(tasks);
    }

    @Test
    public void testShutdownRunningExecutorsWhenEmpty() {
        // Should not throw when no executors are running
        ThreadUtils.shutdownRunningExecutors();
    }

    @Test
    public void testShutdownRunningExecutorsAfterTasks() {
        AtomicInteger counter = new AtomicInteger(0);
        List<Callable<Void>> tasks = List.of(() -> {
            counter.incrementAndGet();
            return null;
        });
        ThreadUtils.executeTasks(tasks);

        // Shutdown should not throw
        ThreadUtils.shutdownRunningExecutors();
    }

    @Test
    public void testShutdownWhileTasksRunning() throws Exception {
        // Submit a long-running task, then shutdown before it completes
        AtomicInteger counter = new AtomicInteger(0);
        List<Callable<Void>> tasks = new ArrayList<>();
        tasks.add(() -> {
            Thread.sleep(5000); // long sleep
            counter.incrementAndGet();
            return null;
        });

        // Run in a separate thread so we can shutdown while it's running
        Thread runner = new Thread(() -> ThreadUtils.executeTasks(tasks));
        runner.start();

        // Brief wait to let the executor get registered
        Thread.sleep(100);

        // Shutdown should interrupt the running task
        ThreadUtils.shutdownRunningExecutors();

        runner.join(2000);
    }
}
