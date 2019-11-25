package ru.nchernetsov.test.pixonic;

import java.time.LocalDateTime;

public class PoisonPillTask extends Task<PoisonPillResult> {

    public PoisonPillTask() {
        super(null, LocalDateTime.now(), PoisonPillResult::new);
    }
}
