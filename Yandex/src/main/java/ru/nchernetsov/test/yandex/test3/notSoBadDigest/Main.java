package ru.nchernetsov.test.yandex.test3.notSoBadDigest;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(availableProcessors - 2);

        DigestWorkerService digestWorkerService = new ConcreteDigestSum();
        DigestService digestService = new DigestService(digestWorkerService);

        List<Callable<byte[]>> callables = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            final byte[] input = {1, 2, 3, 4, 5};
            callables.add(() -> digestService.digest(input));
        }

        List<Future<byte[]>> futures = executorService.invokeAll(callables);

        List<byte[]> results = futures.stream()
            .map(future -> {
                try {
                    return future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                return null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        results.forEach(result -> System.out.println(Arrays.toString(result)));

        System.out.println("State of cache");
        Map<ByteArrayKey, byte[]> cache = DigestCache.getInstance();
        cache.forEach((key, value) -> {
            System.out.printf("key = %s, value = %s\n", key, Arrays.toString(value));
        });

        executorService.shutdown();
    }
}
