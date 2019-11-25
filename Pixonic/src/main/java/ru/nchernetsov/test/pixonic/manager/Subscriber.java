package ru.nchernetsov.test.pixonic.manager;

import ru.nchernetsov.test.pixonic.task.Result;

import java.util.UUID;

public interface Subscriber<V> {

    UUID getUuid();

    void onResult(Result<V> result);
}
