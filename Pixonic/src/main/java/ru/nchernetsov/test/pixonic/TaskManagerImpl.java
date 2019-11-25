package ru.nchernetsov.test.pixonic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nchernetsov.test.pixonic.task.PoisonPillResult;
import ru.nchernetsov.test.pixonic.task.PoisonPillTask;
import ru.nchernetsov.test.pixonic.task.Result;
import ru.nchernetsov.test.pixonic.task.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class TaskManagerImpl implements TaskManager {

    private static final int EXECUTORS_COUNT = Runtime.getRuntime().availableProcessors();

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
    private final BlockingQueue<Result<Object>> resultQueue = new ArrayBlockingQueue<>(100);

    /**
     * Для каждой задачи сохраняем id клиента, от которого она пришла
     */
    private final Map<UUID, UUID> taskClientMap = new HashMap<>();

    /**
     * Сохраняем словарь вида (id клиента => клиент) для отправки ответов подписавшимся клиентам
     */
    private final Map<UUID, Subscriber> subscribers = new HashMap<>();

    private final ExecutorService executorService = Executors.newFixedThreadPool(EXECUTORS_COUNT);

    @Override
    public void start() {
        startExecutionLoop();
        startNotificationLoop();
    }

    @Override
    public <V> void scheduleTask(Task<V> task) {
        UUID taskUuid = task.getUuid();
        UUID clientUuid = task.getClientUUID();
        taskClientMap.put(taskUuid, clientUuid);
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
        subscribers.put(subscriber.getUuid(), subscriber);
    }

    @Override
    public void removeSubscriber(Subscriber subscriber) {
        subscribers.remove(subscriber.getUuid());
    }

    void startExecutionLoop() {
        new Thread(() -> {
            while (true) {
                // выбираем задачи из очереди
                try {
                    Task task = tasksQueue.take();
                    if (task instanceof PoisonPillTask) {
                        log.debug("receive Poison Pill Task => stop execution thread");
                        invokeTask(task);
                        break;
                    }
                    // и выполняем их
                    invokeTask(task);
                } catch (InterruptedException e) {
                    log.debug("executionThread interrupt: exception = {}", e.getMessage());
                    break;
                }
            }
            executorService.shutdown();
        }).start();
    }

    void startNotificationLoop() {
        new Thread(() -> {
            while (true) {
                while (!resultQueue.isEmpty()) {
                    Result<Object> result = resultQueue.poll();
                    if (result instanceof PoisonPillResult) {
                        log.debug("receive Poison Pill => stop notification thread");
                        break;
                    }
                    notifyClient(result);
                }
            }
        }).start();
    }

    private void invokeTask(Task task) {
        executorService.submit(() -> {
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
        });
    }

    private void notifyClient(Result<Object> result) {
        UUID onTaskUuid = result.getOnTask();
        UUID clientUuidToNotify = taskClientMap.get(onTaskUuid);
        if (clientUuidToNotify != null) {
            Subscriber clientToNotify = subscribers.get(clientUuidToNotify);
            if (clientToNotify != null) {
                clientToNotify.onResult(result);
            }
        }
        taskClientMap.remove(onTaskUuid);
    }
}
