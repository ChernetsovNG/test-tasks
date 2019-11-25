package ru.nchernetsov.test.pixonic.task;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * Задача для исполнения
 *
 * @param <V> тип возвращаемого значения
 */
public class Task<V> {

    private final UUID uuid = UUID.randomUUID();

    /**
     * Идентификатор клиента, создавшего данную задачу
     */
    private final UUID clientUUID;

    /**
     * Момент времени, в который нужно начать выполнение задача
     */
    private final LocalDateTime time;

    /**
     * Вычислительная задача, возвращающая значение типа V
     */
    private final Callable<V> task;

    public Task(UUID clientUUID, LocalDateTime time, Callable<V> task) {
        this.clientUUID = clientUUID;
        this.time = time;
        this.task = task;
    }

    public UUID getUuid() {
        return uuid;
    }

    public UUID getClientUUID() {
        return clientUUID;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public Callable<V> getTask() {
        return task;
    }
}
