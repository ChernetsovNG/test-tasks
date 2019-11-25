package ru.nchernetsov.test.pixonic;

import org.junit.Test;
import ru.nchernetsov.test.pixonic.client.SimpleClient;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientTest {

    @Test
    public void clientShouldGetResultInFuture() {
        long deltaMillis = 200L;

        TaskManager taskManager = new TaskManagerImpl();
        SimpleClient client = new SimpleClient(taskManager);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime performTaskTime = now.plus(deltaMillis, ChronoUnit.MILLIS);  // через 200 миллисекунд

        Task<Integer> task = new Task<>(client.getUuid(), performTaskTime, () -> 42);
        client.scheduleTask(task);

        assertThat(client.getResult()).isNull();
        try {
            TimeUnit.MILLISECONDS.sleep(deltaMillis + 50L);  // ждём 250 миллисекунд до получения результата
            Object result = client.getResult();
            assertThat(result).isNotNull();
            assertThat(result).isInstanceOf(Integer.class);
            assertThat((Integer) result).isEqualTo(42);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void severalClientsCreateTasksInDifferentThreadsAndShouldGetTheirResults() {
        TaskManager taskManager = new TaskManagerImpl();

        // Клиенты создают задачи из разных потоков
        ClientThread client1 = new ClientThread(taskManager, 100L, 3);
        ClientThread client2 = new ClientThread(taskManager, 200L, 7);
        ClientThread client3 = new ClientThread(taskManager, 300L, 11);

        // запускаем создание задач в разных потоках
        new Thread(client1).start();
        new Thread(client2).start();
        new Thread(client3).start();

        // вначале результатов нет
        assertThat(client1.getResult()).isNull();
        assertThat(client2.getResult()).isNull();
        assertThat(client3.getResult()).isNull();

        try {
            // через 150 мс первый клиент содержит результат
            TimeUnit.MILLISECONDS.sleep(150L);

            assertThat(client1.getResult()).isNotNull();
            assertThat((Integer) client1.getResult()).isEqualTo(3);
            assertThat(client2.getResult()).isNull();
            assertThat(client3.getResult()).isNull();

            // через 250 мс первые 2 клиента содержат результат
            TimeUnit.MILLISECONDS.sleep(100L);

            assertThat(client1.getResult()).isNotNull();
            assertThat((Integer) client1.getResult()).isEqualTo(3);
            assertThat(client2.getResult()).isNotNull();
            assertThat((Integer) client2.getResult()).isEqualTo(7);
            assertThat(client3.getResult()).isNull();

            // через 350 мс все клиенты содержат результат
            TimeUnit.MILLISECONDS.sleep(100L);

            assertThat(client1.getResult()).isNotNull();
            assertThat((Integer) client1.getResult()).isEqualTo(3);
            assertThat(client2.getResult()).isNotNull();
            assertThat((Integer) client2.getResult()).isEqualTo(7);
            assertThat(client3.getResult()).isNotNull();
            assertThat((Integer) client3.getResult()).isEqualTo(11);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static class ClientThread implements Runnable {

        private final TaskManager taskManager;

        private final long deltaMillis;

        private final int result;

        private final SimpleClient client;

        private ClientThread(TaskManager taskManager, long deltaMillis, int result) {
            this.taskManager = taskManager;
            this.deltaMillis = deltaMillis;
            this.result = result;
            client = new SimpleClient(taskManager);
        }

        @Override
        public void run() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime performTaskTime = now.plus(deltaMillis, ChronoUnit.MILLIS);

            Task<Integer> task = new Task<>(client.getUuid(), performTaskTime, () -> result);
            client.scheduleTask(task);
        }

        Object getResult() {
            return client.getResult();
        }
    }
}
