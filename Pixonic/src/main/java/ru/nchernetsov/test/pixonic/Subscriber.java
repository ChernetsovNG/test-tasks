package ru.nchernetsov.test.pixonic;

import ru.nchernetsov.test.pixonic.task.Result;

import java.util.UUID;

public interface Subscriber {

    UUID getUuid();

    <V> void onResult(Result<V> result);
}
