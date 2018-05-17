package ru.nchernetsov.test.vulkan;

import java.util.OptionalDouble;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.stream.LongStream;

public class HeavyTask implements Callable<Double> {

    // one long is 8 bytes => array size must be ~ 2300 Mb
    private static final int STREAM_SIZE = 300_000_000;

    private final String name;

    HeavyTask(String name) {
        this.name = name;
    }

    @Override
    public Double call() {
        System.out.println("Init: " + name);

        final Random random = new Random();

        OptionalDouble average = LongStream
            .generate(random::nextLong).limit(STREAM_SIZE)
            .map(Math::abs)
            .mapToDouble(Math::sqrt).average();

        System.out.println("Complete: " + name);

        if (average.isPresent()) {
            return average.getAsDouble();
        } else {
            throw new RuntimeException("Error in caclulations in: " + name);
        }
    }

}
