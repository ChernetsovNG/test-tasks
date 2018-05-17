package ru.nchernetsov.test.vulkan.examples;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

/**
 * Тест работы CompletableFuture в сочетании с ForkJoinPool
 */
public class ForkJoinPoolAndCompletableFutureTest {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ForkJoinPool pool = new ForkJoinPool(5);

        CompletableFuture<String> completableFuture1 = CompletableFuture.supplyAsync(
            () -> {
                System.out.println(Thread.currentThread().getName());
                return "Running completable future";
            }, pool);

        CompletableFuture<String> completableFuture2 = completableFuture1.thenApplyAsync(
            r -> {
                System.out.println(Thread.currentThread().getName());
                return r + ". Hello!";
            }, pool);

        System.out.println(completableFuture1.get());
        System.out.println(completableFuture2.get());

        System.out.println(Thread.currentThread().getName());
        System.out.println("Main method");
    }

}
