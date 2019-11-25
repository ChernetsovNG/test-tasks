package ru.nchernetsov.test.pixonic;

import ru.nchernetsov.test.pixonic.client.Client;
import ru.nchernetsov.test.pixonic.client.TimerClient;
import ru.nchernetsov.test.pixonic.manager.TaskManager;
import ru.nchernetsov.test.pixonic.manager.TaskManagerImpl;
import ru.nchernetsov.test.pixonic.task.PoisonPillTask;
import ru.nchernetsov.test.pixonic.task.Task;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * Пример планирования и исполнения задач
 */
public class TimerExample {

    public static void main(String[] args) {
        // Создаём менеджера задач и клиента
        TaskManager taskManager = new TaskManagerImpl();
        Client<String> timerClient = new TimerClient(taskManager);

        // подписываемся в клиенте на результат
        taskManager.addSubscriber(timerClient);

        // запускаем потоки выполнения менеджерва
        taskManager.start();

        LocalDateTime startTime = LocalDateTime.now();

        int workTimeSeconds = 10;

        // Создаём задачи из 2-х потоков
        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < workTimeSeconds; i += 2) {
                LocalDateTime performTime = startTime.plus(i, ChronoUnit.SECONDS);
                Task<String> timerTask = new Task<>(timerClient.getUuid(), performTime, () -> LocalDateTime.now().toString());
                taskManager.scheduleTask(timerTask);
            }
        });

        Thread thread2 = new Thread(() -> {
            for (int i = 1; i < workTimeSeconds; i += 2) {
                LocalDateTime performTime = startTime.plus(i, ChronoUnit.SECONDS);
                Task<String> timerTask = new Task<>(timerClient.getUuid(), performTime, () -> LocalDateTime.now().toString());
                taskManager.scheduleTask(timerTask);
            }
        });

        thread1.start();
        thread2.start();

        // Наблюдаем в консоли результат...
        try {
            TimeUnit.SECONDS.sleep(workTimeSeconds);
        } catch (InterruptedException e) {
            throw new RuntimeException();
        }

        // Останавливаем выполнение
        taskManager.scheduleTask(new PoisonPillTask());
    }
}
