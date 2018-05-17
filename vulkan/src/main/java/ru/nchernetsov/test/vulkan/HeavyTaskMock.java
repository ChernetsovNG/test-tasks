package ru.nchernetsov.test.vulkan;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Imitation of heavy task by using delay
 */
public class HeavyTaskMock implements Callable<Double> {

    private static final int DELAY_MS = 3000;
    private final String name;

    HeavyTaskMock(String name) {
        this.name = name;
    }

    @Override
    public Double call() {
        System.out.println("Init: " + name);
        try {
            TimeUnit.MILLISECONDS.sleep(DELAY_MS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Complete: " + name);
        return new Random().nextDouble();
    }

}
