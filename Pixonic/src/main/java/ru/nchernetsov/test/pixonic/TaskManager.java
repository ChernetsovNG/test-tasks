package ru.nchernetsov.test.pixonic;

import java.util.Queue;
import java.util.UUID;

public interface TaskManager extends Publisher {

    /**
     * Отправить на выполнение задачу
     *
     * @param task задача для выполнения
     * @param <V>  тип результата
     */
    <V> void scheduleTask(Task<V> task);

    /**
     * Получить список запланированных задач (в порядке планирования)
     */
    Queue<UUID> getScheduledTasks();

    /**
     * Получить очередь результатов выполнения задач (в порядке выполнения)
     */
    Queue<Object> getResults();
}
