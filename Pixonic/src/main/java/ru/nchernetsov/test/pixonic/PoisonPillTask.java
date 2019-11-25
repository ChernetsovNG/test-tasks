package ru.nchernetsov.test.pixonic;

import java.time.LocalDateTime;

public class PoisonPillTask extends Task<Void> {

    public PoisonPillTask() {
        super(null, LocalDateTime.now(), () -> null);
    }
}
