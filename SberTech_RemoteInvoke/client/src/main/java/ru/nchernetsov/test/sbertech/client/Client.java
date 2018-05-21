package ru.nchernetsov.test.sbertech.client;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nchernetsov.test.sbertech.client.exception.RemoteCallException;
import ru.nchernetsov.test.sbertech.client.handler.ConnectAnswerHandler;
import ru.nchernetsov.test.sbertech.client.handler.ConnectAnswerHandlerImpl;
import ru.nchernetsov.test.sbertech.client.handler.MethodInvokeAnswerHandler;
import ru.nchernetsov.test.sbertech.client.handler.MethodInvokeAnswerHandlerImpl;
import ru.nchernetsov.test.sbertech.client.utils.ClientUtils;
import ru.nchernetsov.test.sbertech.client.utils.RandomString;
import ru.nchernetsov.test.sbertech.common.CommonData;
import ru.nchernetsov.test.sbertech.common.channel.SocketClientChannel;
import ru.nchernetsov.test.sbertech.common.channel.SocketClientManagedChannel;
import ru.nchernetsov.test.sbertech.common.enums.ConnectOperation;
import ru.nchernetsov.test.sbertech.common.enums.MethodInvokeStatus;
import ru.nchernetsov.test.sbertech.common.message.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static ru.nchernetsov.test.sbertech.common.CommonData.DEFAULT_SERVER_PORT;
import static ru.nchernetsov.test.sbertech.common.CommonData.SERVER_ADDRESS;
import static ru.nchernetsov.test.sbertech.common.enums.MethodInvokeStatus.OK_RESULT;
import static ru.nchernetsov.test.sbertech.common.enums.MethodInvokeStatus.OK_VOID;
import static ru.nchernetsov.test.sbertech.common.utils.StringCrypter.stringCrypter;

/**
 * Основной класс клиента
 */
public class Client implements Addressee {
    private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    private static final String HOST = "localhost";

    private static final int PAUSE_MS = 223;
    private static final int THREADS_NUMBER = 2;

    private final ExecutorService executor = Executors.newFixedThreadPool(THREADS_NUMBER);

    // ожидаем установления соединения с сервером, после чего снимаем блокировку обработки сообщений
    private final CountDownLatch handshakeLatch = new CountDownLatch(1);

    // для каждого вызова метода remoteCall создаём отдельный объект CountDownLatch
    private final Map<UUID, CountDownLatch> remoteCallLatches = new ConcurrentHashMap<>();
    private final Map<UUID, Pair<MethodInvokeStatus, Object>> remoteCallResults = new ConcurrentHashMap<>();

    private final ConnectAnswerHandler connectAnswerHandler;
    private final MethodInvokeAnswerHandler methodInvokeAnswerHandler;

    private SocketClientChannel client;

    private final Address address;

    public static void main(String... args) throws InterruptedException {
        Integer serverPortNum = null;
        if (args.length > 0) {
            serverPortNum = Integer.parseInt(args[0]);
        }
        Client client = new Client();
        client.start(serverPortNum);
    }

    private Client() {
        String macAddresses = ClientUtils.getMacAddress();  // MAC-адреса клинта
        // на случай запуска нескольких клиентов на одном хосте ещё добавим случайную строку, чтобы адреса были разные
        RandomString randomStringGenerator = new RandomString(10);
        String randomString = randomStringGenerator.nextString();

        // String clientAddress = stringCrypter.encrypt(randomString + macAddresses);
        String clientAddress = randomString + macAddresses;

        address = new Address(clientAddress);

        connectAnswerHandler = new ConnectAnswerHandlerImpl(this);
        methodInvokeAnswerHandler = new MethodInvokeAnswerHandlerImpl(this);
    }

    private void start(Integer serverPortNum) throws InterruptedException {
        LOG.info("Client process started");
        try {
            if (serverPortNum != null) {
                client = new SocketClientManagedChannel(HOST, serverPortNum);
            } else {
                client = new SocketClientManagedChannel(HOST, DEFAULT_SERVER_PORT);
            }
        } catch (IOException e) {
            LOG.error("Error to start Client: {}", e.getMessage());
        }
        client.init();

        // запускаем циклы обработки сообщений
        executor.submit(this::handshakeOnServer);
        executor.submit(this::serverMessageHandle);

        // Блокируемся до установления соединения с сервером
        handshakeLatch.await();

        testRun1();
        testRun2();

        // пока у нас есть необработанные запросы, не останавливаем основной Thread клиента
        while (!remoteCallLatches.isEmpty()) {
            Thread.sleep(PAUSE_MS);
        }
        close();
    }

    private void testRun1() {
        // отправляем сообщение об удалённом вызове метода
        try {
            Object remoteCallAnswer = remoteCall("DateService", "sleep", 5_000L);
            LOG.info("Method result: {}", remoteCallAnswer);
        } catch (RemoteCallException e) {
            LOG.error("RemoteCallException: {}", e.getMessage());
        }
    }

    private void testRun2() {
        // отправляем сообщение об удалённом вызове метода
        try {
            Object remoteCallAnswer = remoteCall("MathService", "multiply", 3, 9);
            LOG.info("Method result: {}", remoteCallAnswer);
        } catch (RemoteCallException e) {
            LOG.error("RemoteCallException: {}", e.getMessage());
        }

        // тестируем работу в многопоточном режиме
        for (int i = 0; i < 10; i++) {
            new Thread(new Caller(this)).start();
        }
    }

    private static class Caller implements Runnable {
        private static final Logger logger = LoggerFactory.getLogger(Caller.class);
        private final Client client;

        Caller(Client client) {
            this.client = client;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    client.remoteCall("DateService", "sleep", 1000L);
                    logger.info("Current Date is: {}",
                        client.remoteCall("DateService", "getCurrentDate"));
                } catch (RemoteCallException e) {
                    logger.error("RemoteCallException: {}", e.getMessage());
                }
            }
        }
    }

    // Обработка ответов от сервера
    private void serverMessageHandle() {
        try {
            while (true) {
                Message serverMessage = client.take();
                if (serverMessage != null) {
                    if (serverMessage.isClass(ConnectAnswerMessage.class)) {
                        connectAnswerHandler.handleMessage((ConnectAnswerMessage) serverMessage);
                    } else if (serverMessage.isClass(MethodInvokeAnswerMessage.class)) {
                        methodInvokeAnswerHandler.handleMessage((MethodInvokeAnswerMessage) serverMessage);
                    } else {
                        LOG.warn("Получено сообщение необрабатываемого класса. Message: {}", serverMessage);
                    }
                } else {
                    TimeUnit.MILLISECONDS.sleep(PAUSE_MS);
                }
            }
        } catch (InterruptedException e) {
            LOG.error(e.getMessage());
        }
    }

    private void handshakeOnServer() {
        Message handshakeDemandMessage = new ConnectOperationMessage(address, SERVER_ADDRESS, ConnectOperation.HANDSHAKE);
        connectAnswerHandler.setHandshakeMessageUuid(handshakeDemandMessage.getUuid());
        client.send(handshakeDemandMessage);
        LOG.debug("Отправлено сообщение об установлении соединения на сервер");
    }

    /**
     * Основной метод для вызова remote метода
     *
     * @param serviceName - название сервиса
     * @param methodName  - имя метода
     * @param params      - массив параметров метода
     * @return - результат работы удалённого метода (или null в случае void метода)
     * @throws RemoteCallException - если вызов удалённого метода завершился неудачей
     */
    public Object remoteCall(String serviceName, String methodName, Object... params) throws RemoteCallException {
        MethodInvokeDemandMessage demandMessage = new MethodInvokeDemandMessage(address, SERVER_ADDRESS, serviceName, methodName, params);

        methodInvokeAnswerHandler.addDemandMessage(demandMessage);
        client.send(demandMessage);
        LOG.info("Отправлен запрос на удалённый вызов метода. Сервис: {}, метод: {}, параметры: {}",
            serviceName, methodName, Arrays.toString(params));

        blockRemoteCall(demandMessage.getUuid());

        Pair<MethodInvokeStatus, Object> remoteMethodResult = remoteCallResults.remove(demandMessage.getUuid());
        MethodInvokeStatus methodInvokeStatus = remoteMethodResult.getKey();
        if (!isStatusNormal(methodInvokeStatus)) {
            LOG.error("Вызов метода завершился неудачей. Статус вызова: {}", methodInvokeStatus);
            throw new RemoteCallException("RemoteCallException. Status: " + methodInvokeStatus);
        }
        return remoteMethodResult.getValue();
    }

    private boolean isStatusNormal(MethodInvokeStatus methodInvokeStatus) {
        return methodInvokeStatus.equals(OK_RESULT) || methodInvokeStatus.equals(OK_VOID);
    }

    // блокируем метод remoteCall до тех пор, пока не будет получен ответ
    private void blockRemoteCall(UUID demandMessageUUID) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        remoteCallLatches.put(demandMessageUUID, countDownLatch);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            LOG.error(e.getMessage());
        }
        // после снятия блокировки удаляем CountDownLatch для данного запроса
        remoteCallLatches.remove(demandMessageUUID);
    }

    // снимаем блокировку после установления соединения с сервером
    public void unlockMessageHandle() {
        handshakeLatch.countDown();
    }

    // после получения ответа от сервера снимаем блокировку для сообщения
    public void unblockMessageLatch(UUID demandMessageUUID, MethodInvokeStatus methodInvokeStatus, Object remoteCallResult) {
        CountDownLatch countDownLatch = remoteCallLatches.get(demandMessageUUID);
        // сохраняем результат вызова метода в виде пары <статус вызова - результат>
        remoteCallResults.put(demandMessageUUID, new ImmutablePair<>(methodInvokeStatus, remoteCallResult));
        if (countDownLatch != null) {
            countDownLatch.countDown();
        }
    }

    private void close() {
        try {
            client.close();
            executor.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Address getAddress() {
        return address;
    }

}
