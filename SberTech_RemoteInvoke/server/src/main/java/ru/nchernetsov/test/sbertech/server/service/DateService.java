package ru.nchernetsov.test.sbertech.server.service;

import java.time.LocalDateTime;

public class DateService {
    public void sleep(Long millis) throws InterruptedException {
        Thread.sleep(millis);
    }

    public LocalDateTime getCurrentDate() {
        return LocalDateTime.now();
    }

}
