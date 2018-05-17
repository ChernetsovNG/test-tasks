package ru.nchernetsov.test.vulkan;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;
import java.util.stream.Collectors;

// -Xmx10g -Xms10g
public class Solution {
    private static final int TASKS_COUNT = 20;
    private static final int THREADS_COUNT = 5;  // 5 CompletableFutures on 5 threads

    public static void main(String[] args) {
        Solution solution = new Solution();
        solution.executeSolution();
    }

    private void executeSolution() {
        long start = System.nanoTime();

        List<Callable<Double>> tasks = createTasks();
        double[] tasksResults = executeAllTasks(tasks);

        System.out.println();
        System.out.println("Results for all tasks:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println("task: " + i + " ; result = " + tasksResults[i]);
        }

        long finish = System.nanoTime();
        System.out.println("Time to execute all 20 tasks is: " + (float) (finish - start) / 1e6 + " ms");
    }

    /**
     * Create tasks for execution. Numbers from zero
     *
     * @return - list of tasks
     */
    private List<Callable<Double>> createTasks() {
        final List<Callable<Double>> tasks = new ArrayList<>();
        for (int i = 0; i < TASKS_COUNT; i++) {
            final int finalI = i;
            tasks.add(() -> new HeavyTask("task " + finalI).call());
        }
        return tasks;
    }

    /**
     * Execute all tasks using ForkJoinPool and CompletableFuture
     *
     * @param tasks - list of tasks
     * @return - calculation results. One result for each task
     */
    private double[] executeAllTasks(List<Callable<Double>> tasks) {
        ForkJoinPool pool = new ForkJoinPool(THREADS_COUNT);

        final double[] tasksResults = new double[tasks.size()];

        // Create 5 CompletableFuture, by 4 tasks on each
        CompletableFuture[] completableFutures = new CompletableFuture[5];
        // convert tasks into supplier objects
        List<Supplier<Double>> supplierTasks = tasks.stream()
            .map(this::taskToSupplier)
            .collect(Collectors.toList());
        int j = 0;
        for (int i = 0; i < 20; i += 4) {
            completableFutures[j] = createCompletableFutureForFiveTasks(pool, supplierTasks, tasksResults, i);
            j++;
        }

        // run all CompletableFutures and wait until done
        CompletableFuture<Void> combineCompletableFuture = CompletableFuture.allOf(completableFutures);
        combineCompletableFuture.join();  // blocks here

        // return calculation results
        return tasksResults;
    }

    /**
     * Create one CompletableFuture for 4 tasks (by condition)
     *
     * @param pool         - ExecutorService for tasks execution
     * @param tasks        - list of tasks
     * @param tasksResults - array to save tasks results
     * @param fromIndex    - first task index
     * @return - CompletableFuture for 4 tasks starting from index in list of tasks
     */
    private CompletableFuture<Double> createCompletableFutureForFiveTasks(
        final ForkJoinPool pool, final List<Supplier<Double>> tasks, final double[] tasksResults, int fromIndex) {

        return CompletableFuture.supplyAsync(tasks.get(fromIndex), pool)
            .thenComposeAsync(res -> {
                tasksResults[fromIndex] = res;
                return CompletableFuture.supplyAsync(tasks.get(fromIndex + 1), pool);
            })
            .thenComposeAsync(res -> {
                tasksResults[fromIndex + 1] = res;
                return CompletableFuture.supplyAsync(tasks.get(fromIndex + 2), pool);
            })
            .thenComposeAsync(res -> {
                tasksResults[fromIndex + 2] = res;
                return CompletableFuture.supplyAsync(tasks.get(fromIndex + 3), pool);
            })
            .thenApplyAsync(res -> {
                tasksResults[fromIndex + 3] = res;
                return res;
            }, pool)
            .exceptionally(throwable -> Double.NaN);
    }

    // for masking checked exception (it's convenient to use in lambda-expressions)
    private Supplier<Double> taskToSupplier(Callable<Double> task) {
        return () -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

}
