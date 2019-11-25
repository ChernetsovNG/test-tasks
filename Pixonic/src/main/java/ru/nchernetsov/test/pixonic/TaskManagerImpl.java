package ru.nchernetsov.test.pixonic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

public class TaskManagerImpl implements TaskManager {

    private final Logger log = LoggerFactory.getLogger(TaskManager.class);

    /**
     * Очередь задач для выполнения:
     * очередь с приоритетом использует Comparator для упорядочивания задач в соответствии с условием:
     * задачи должны выполняться в порядке согласно значению LocalDateTime
     * либо в порядке прихода события для равных LocalDateTime
     */
    private final BlockingQueue<Task> tasksQueue = new PriorityBlockingQueue<>(100, Comparator.comparing(Task::getTime));

    /**
     * Очередь результатов выполненных задач
     */
    private final BlockingQueue<Result> resultQueue = new ArrayBlockingQueue<>(100);

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
    public Queue<Object> getResults() {
        return resultQueue.stream()
                .map(Result::getResult)
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

    public void startExecutionLoop() {
        Thread executionThread = new Thread(() -> {
            while (true) {
                // выбираем задачи из очереди
                try {
                    Task task = tasksQueue.take();
                    if (task instanceof PoisonPillTask) {
                        log.debug("receive Poison Pill => stop execution thread");
                        break;
                    }
                    // и выполняем их
                    invokeTask(task);
                } catch (InterruptedException e) {
                    log.debug("executionThread interrupt: exception = {}", e.getMessage());
                    break;
                }
            }
        });
        executionThread.start();
    }

    private void invokeTask(Task task) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime performTime = task.getTime();
        Callable callable = task.getTask();
        try {
            // если время выполнения задачи уже прошло, то сразу выполняем, иначе
            // ждём какое-то время, а затем выполняем задачу
            if (performTime.isAfter(now)) {
                Duration waitDuration = Duration.between(now, performTime);
                long waitMillis = waitDuration.toMillis();
                Thread.sleep(waitMillis);
            }
            Object callResult = callable.call();
            Result<Object> result = new Result<>(task.getUuid(), callResult);
            resultQueue.offer(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
