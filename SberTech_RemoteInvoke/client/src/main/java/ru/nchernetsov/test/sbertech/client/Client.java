package ru.nchernetsov.test.sbertech.client;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nchernetsov.test.sbertech.client.controller.ConnectController;
import ru.nchernetsov.test.sbertech.client.controller.MethodInvokeController;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static ru.nchernetsov.test.sbertech.common.CommonData.SERVER_ADDRESS;
import static ru.nchernetsov.test.sbertech.common.enums.MethodInvokeStatus.OK_RESULT;

public class Client implements Addressee {
    private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    private static final String HOST = "localhost";

    private static final int PAUSE_MS = 223;
    private static final int THREADS_NUMBER = 1;

    private ExecutorService executor = Executors.newFixedThreadPool(THREADS_NUMBER);

    // ожидаем установления соединения с сервером, после чего снимаем блокировку обработки сообщений
    private final CountDownLatch handshakeLatch = new CountDownLatch(1);

    // для каждого вызова метода remoteCall создаём отдельную CountDownLatch
    private final Map<UUID, CountDownLatch> remoteCallLatches = new HashMap<>();
    private final Map<UUID, Pair<MethodInvokeStatus, Object>> remoteCallResults = new HashMap<>();

    private final ConnectController connectController;
    private final MethodInvokeController methodInvokeController;

    private final ConnectAnswerHandler connectAnswerHandler;
    private final MethodInvokeAnswerHandler methodInvokeAnswerHandler;

    private SocketClientChannel client;

    private final Address address;

    public static void main(String... args) throws InterruptedException {
        Integer serverPortNum = null;
        if (args.length > 0) {
            serverPortNum = Integer.parseInt(args[0]);
        }
        Client client = new Client(new ConnectController(), new MethodInvokeController());
        client.start(serverPortNum);
    }

    public Client(ConnectController connectController, MethodInvokeController methodInvokeController) {
        String macAddresses = ClientUtils.getMacAddress();  // MAC-адреса клинта
        // на случай запуска нескольких клиентов на одном хосте ещё добавим случайную строку, чтобы адреса были разные
        RandomString randomStringGenerator = new RandomString(10);
        String randomString = randomStringGenerator.nextString();

        String clientAddress = StringCrypter.stringCrypter.encrypt(randomString + macAddresses);

        this.address = new Address(clientAddress);

        this.connectAnswerHandler = new ConnectAnswerHandlerImpl(connectController);
        this.methodInvokeAnswerHandler = new MethodInvokeAnswerHandlerImpl(methodInvokeController);

        this.connectController = connectController;
        this.methodInvokeController = methodInvokeController;

        connectController.setModel(this);
        methodInvokeController.setModel(this);
    }

    public void start(Integer serverPortNum) throws InterruptedException {
        LOG.info("Client process started");
        try {
            if (serverPortNum != null) {
                client = new SocketClientManagedChannel(HOST, serverPortNum);
            } else {
                client = new SocketClientManagedChannel(HOST, CommonData.DEFAULT_SERVER_PORT);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        client.init();

        executor.submit(this::handshakeOnServer);
        executor.submit(this::serverMessageHandle);

        // После установления соединения с сервером снимаем блокировку
        handshakeLatch.await();

        // отправляем несколько сообщений об удалённом вызове метода
        Object remoteCallAnswer = remoteCall("DateService", "getCurrentDate", new Object[]{});
        LOG.info("Method result: ", remoteCallAnswer);

        TimeUnit.MILLISECONDS.sleep(1000);
        close();
    }

    // Обработка ответов от сервера
    private void serverMessageHandle() {
        try {
            while (true) {
                Message serverMessage = client.take();
                if (serverMessage != null) {
                    if (serverMessage.isClass(ConnectAnswerMessage.class)) {
                        connectAnswerHandler.handleMessage((ConnectAnswerMessage) serverMessage);
                    } else if (serverMessage.isClass(AnswerMessage.class)) {
                        methodInvokeAnswerHandler.handleMessage((AnswerMessage) serverMessage);
                    } else {
                        LOG.debug("Получено сообщение необрабатываемого класса. Message: {}", serverMessage);
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
        Message handshakeDemandMessage = new ConnectOperationMessage(address, SERVER_ADDRESS, ConnectOperation.HANDSHAKE, null);
        connectAnswerHandler.setHandshakeMessageUuid(handshakeDemandMessage.getUuid());
        client.send(handshakeDemandMessage);
        LOG.debug("Отправлено сообщение об установлении соединения на сервер");
    }

    public void unlockMessageHandle() {
        handshakeLatch.countDown();
    }

    public Object remoteCall(String serviceName, String methodName, Object[] params) {
        DemandMessage demandMessage = new DemandMessage(address, SERVER_ADDRESS, serviceName, methodName, params);
        methodInvokeAnswerHandler.addDemandMessage(demandMessage);
        client.send(demandMessage);
        LOG.info("Отправлен запрос на удалённый вызов метода. Сервис: {}, метод: {}, параметры: {}",
            serviceName, methodName, Arrays.toString(params));
        blockRemoteCall(demandMessage.getUuid());

        Pair<MethodInvokeStatus, Object> remoteMethodResult = remoteCallResults.remove(demandMessage.getUuid());
        MethodInvokeStatus methodInvokeStatus = remoteMethodResult.getKey();
        if (!methodInvokeStatus.equals(OK_RESULT)) {
            LOG.error("Вызов метода завершился неудачей. Статус вызова: {}", methodInvokeStatus);
            throw new RemoteCallException();
        }
        return remoteMethodResult.getValue();
    }

    private void blockRemoteCall(UUID demandMessageUUID) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        remoteCallLatches.put(demandMessageUUID, countDownLatch);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            LOG.error(e.getMessage());
        }
        remoteCallLatches.remove(demandMessageUUID);
    }

    // после получения ответа от сервева отпускаем блокировку для сообщения
    public void unblockLatchAndGetResult(UUID demandMessageUUID, MethodInvokeStatus methodInvokeStatus, Object remoteCallResult) {
        CountDownLatch countDownLatch = remoteCallLatches.get(demandMessageUUID);
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
