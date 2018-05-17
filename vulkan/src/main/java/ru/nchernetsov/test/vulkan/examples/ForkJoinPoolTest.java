package ru.nchernetsov.test.vulkan.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * Тест работы ForkJoinPool
 */
public class ForkJoinPoolTest {
    private static final int SLEEP_TIME_MILLIS = 1000;
    private static final int PARALLELISM = 2;
    private static final int NUMBER_TO_SUM = 10;
    private static final int SUBTASKS_COUNT = 2;

    private final Queue<Integer> sequentialQueue = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) {
        new ForkJoinPoolTest().start();
    }

    private void start() {
        for (int i = 1; i < NUMBER_TO_SUM; i++) {
            sequentialQueue.add(i);
        }

        ForkJoinPool forkJoinPool = new ForkJoinPool(PARALLELISM);
        ForkJoinTask<Integer> task = new LoadRecursive();

        long start = System.currentTimeMillis();
        Integer result = forkJoinPool.invoke(task);

        System.out.println("Done in " + (System.currentTimeMillis() - start) + "ms : " + result);
    }

    private class LoadRecursive extends RecursiveTask<Integer> {
        @Override
        protected Integer compute() {
            Integer item = sequentialQueue.poll();
            if (item != null) {
                System.out.println(Thread.currentThread().getName() + ": add " + item);
                sleep();

                List<LoadRecursive> tasks = new ArrayList<>();

                // fork
                for (int i = 0; i < SUBTASKS_COUNT; i++) {
                    LoadRecursive subTask = new LoadRecursive();
                    subTask.fork();
                    tasks.add(subTask);
                }

                // join
                Integer result = item + tasks.stream().mapToInt(ForkJoinTask::join).sum();

                System.out.println(Thread.currentThread().getName() + ": result: " + result);
                return result;
            }
            return 0;
        }
    }

    private void sleep() {
        try {
            Thread.sleep(SLEEP_TIME_MILLIS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
