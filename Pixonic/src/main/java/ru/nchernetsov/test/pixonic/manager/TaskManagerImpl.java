package ru.nchernetsov.test.pixonic.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nchernetsov.test.pixonic.task.PoisonPillResult;
import ru.nchernetsov.test.pixonic.task.PoisonPillTask;
import ru.nchernetsov.test.pixonic.task.Result;
import ru.nchernetsov.test.pixonic.task.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class TaskManagerImpl implements TaskManager {

    private static final Logger log = LoggerFactory.getLogger(TaskManager.class);

    private static final int EXECUTORS_COUNT = Runtime.getRuntime().availableProcessors();

    /**
     * Максимальный размер очереди задач (т.к. очередь PriorityBlockingQueue растёт неограниченно,
     * то мы её ограничим принудительно, чтобы не исчерпалась память)
     */
    private final int tasksMaxCount;

    /**
     * Очередь задач для выполнения:
     * очередь с приоритетом использует Comparator для упорядочивания задач в соответствии с условием:
     * задачи должны выполняться в порядке согласно значению LocalDateTime
     * либо в порядке прихода события для равных LocalDateTime
     */
    private final BlockingQueue<PerformTask> tasksQueue;

    /**
     * Очередь результатов выполненных задач
     */
    private final BlockingQueue<Result<Object>> resultQueue;

    /**
     * Для каждой задачи сохраняем id клиента, от которого она пришла
     */
    private final Map<UUID, UUID> taskClientMap = new ConcurrentHashMap<>();

    /**
     * Сохраняем словарь вида (id клиента => клиент) для отправки ответов подписавшимся клиентам
     */
    private final Map<UUID, Subscriber> subscribers = new ConcurrentHashMap<>();

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(EXECUTORS_COUNT);

    public TaskManagerImpl() {
        this(100, 1000);
    }

    public TaskManagerImpl(int capacity, int tasksMaxCount) {
        tasksQueue = new PriorityBlockingQueue<>(capacity);
        resultQueue = new ArrayBlockingQueue<>(capacity);
        this.tasksMaxCount = tasksMaxCount;
    }

    @Override
    public void start() {
        executionLoop();
        notificationLoop();
    }

    @Override
    public <V> boolean scheduleTask(Task<V> task) {
        UUID taskUuid = task.getUuid();
        UUID clientUuid = task.getClientUuid();
        if (clientUuid != null) {
            taskClientMap.put(taskUuid, clientUuid);
        }
        if (tasksQueue.size() >= tasksMaxCount) {
            return false;
        }
        return tasksQueue.offer(new PerformTask<>(task));
    }

    @Override
    public Queue<UUID> getScheduledTasks() {
        BlockingQueue<PerformTask> copy = new PriorityBlockingQueue<>(tasksQueue);
        Queue<UUID> result = new ArrayDeque<>();
        while (!copy.isEmpty()) {
            result.offer(copy.poll().task.getUuid());
        }
        return result;
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

    public void executionLoop() {
        new Thread(() -> {
            while (true) {
                // выбираем задачи из очереди
                try {
                    Task task = tasksQueue.take().task;
                    // и выполняем их
                    invokeTask(task);
                    if (task instanceof PoisonPillTask) {
                        log.debug("receive PoisonPill Task => stop execution thread");
                        break;
                    }
                } catch (InterruptedException e) {
                    log.debug("executionThread interrupt: exception = {}", e.getMessage());
                    break;
                }
            }
        }).start();
    }

    void notificationLoop() {
        new Thread(() -> {
            while (true) {
                try {
                    Result<Object> result = resultQueue.take();
                    if (result.getResult() instanceof PoisonPillResult) {
                        log.debug("receive PoisonPill Result => stop notification thread");
                        break;
                    }
                    notifyClient(result);
                } catch (InterruptedException e) {
                    log.debug("notificationThread interrupt: exception = {}", e.getMessage());
                    break;
                }
            }
            executorService.shutdown();
        }).start();
    }

    private void invokeTask(final Task task) {
        // если время выполнения задачи уже прошло, то сразу выполняем задачу, иначе планируем её выполнение в будущем
        long delayMillis = Duration.between(LocalDateTime.now(), task.getTime()).toMillis();
        // при delayMillis < 0 задача будет выполнена сразу же (с нулевой задержкой), см.
        // java.util.concurrent.ScheduledThreadPoolExecutor.triggerTime(long, java.util.concurrent.TimeUnit)
        executorService.schedule(() -> {
            try {
                Object callResult = task.getTask().call();
                Result<Object> result = new Result<>(task.getUuid(), callResult);
                resultQueue.offer(result);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, delayMillis, TimeUnit.MILLISECONDS);
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

    /**
     * Вспомогатльный класс, добавляющий к Task ещё последовательность добавления задачи в очередь
     */
    private static final class PerformTask<V> implements Comparable<PerformTask<V>> {

        /**
         * Для обеспечения правильного упорядочивания задач с одинаковым временем планирования в PriorityBlockingQueue
         */
        private static final AtomicLong seq = new AtomicLong(0);

        /**
         * Последовательность добавления задачи в очередь (для сортировки)
         */
        private final long seqNum;

        private final Task<V> task;

        PerformTask(Task<V> task) {
            this.seqNum = seq.getAndIncrement();
            this.task = task;
        }

        @Override
        public int compareTo(PerformTask<V> other) {
            int res = this.task.getTime().compareTo(other.task.getTime());
            if (res == 0 && other != this) {
                res = seqNum < other.seqNum ? -1 : 1;
            }
            return res;
        }
    }
}
