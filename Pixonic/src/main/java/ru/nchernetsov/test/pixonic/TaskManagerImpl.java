package ru.nchernetsov.test.pixonic;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

public class TaskManagerImpl implements TaskManager {

    /**
     * Очередь с приоритетом использует компаратор для упорядочивания задач в соответствии с условием:
     * задачи должны выполняться в порядке согласно значению LocalDateTime
     * либо в порядке прихода события для равных LocalDateTime
     */
    private final PriorityBlockingQueue<Task> tasksQueue = new PriorityBlockingQueue<>(100, Comparator.comparing(Task::getTime));

    @Override
    public <V> void scheduleTask(Task<V> task) {
        tasksQueue.offer(task);
    }

    @Override
    public Queue<UUID> getScheduledTasks() {
        return tasksQueue.stream()
                .map(Task::getUuid)
                .collect(Collectors.toCollection(ArrayDeque::new));
    }

    @Override
    public void addSubscriber(Subscriber subscriber) {

    }

    @Override
    public void removeSubscriber(Subscriber subscriber) {

    }

    @Override
    public void notifySubscribers() {

    }
}
