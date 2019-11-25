package ru.nchernetsov.test.pixonic.task;

import java.util.UUID;

/**
 * Результат выполнения задачи
 * @param <V> тип результата
 */
public class Result<V> {

    /**
     * Задача, результатом выполнения которой является объект настоящего типа
     */
    private final UUID onTask;

    /**
     * Результат выполнения задачи с идентификатором onTask
     */
    private final V result;

    public Result(UUID onTask, V result) {
        this.onTask = onTask;
        this.result = result;
    }

    public UUID getOnTask() {
        return onTask;
    }

    public V getResult() {
        return result;
    }
}
