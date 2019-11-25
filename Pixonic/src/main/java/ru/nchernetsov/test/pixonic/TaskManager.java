package ru.nchernetsov.test.pixonic;

public interface TaskManager extends Publisher {

    /**
     * Отправить на выполнение задачу
     *
     * @param task задача для выполнения
     * @param <V>  тип результата
     */
    <V> void scheduleTask(Task<V> task);
}
