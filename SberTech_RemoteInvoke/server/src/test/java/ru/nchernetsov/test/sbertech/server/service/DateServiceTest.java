package ru.nchernetsov.test.sbertech.server.service;

import org.junit.jupiter.api.Test;

public class DateServiceTest {

    @Test
    public void dateServiceTest() throws InterruptedException {
        DateService dateService = new DateService();

        Thread thread1 = new Thread(() -> {
            try {
                dateService.sleep(1000L);
                System.out.println("Wake up, Neo");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread thread2 = new Thread(() ->
            System.out.println(dateService.getCurrentDate())
        );

        System.out.println("Start threads");
        thread1.start();
        thread2.start();

        Thread.sleep(2000);
    }
}
