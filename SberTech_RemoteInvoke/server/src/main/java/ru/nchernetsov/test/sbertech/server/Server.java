package ru.nchernetsov.test.sbertech.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nchernetsov.test.sbertech.common.channel.MessageChannel;
import ru.nchernetsov.test.sbertech.common.channel.SocketClientChannel;
import ru.nchernetsov.test.sbertech.common.enums.ConnectOperation;
import ru.nchernetsov.test.sbertech.common.message.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

import static ru.nchernetsov.test.sbertech.common.CommonData.DEFAULT_SERVER_PORT;
import static ru.nchernetsov.test.sbertech.common.CommonData.SERVER_ADDRESS;
import static ru.nchernetsov.test.sbertech.common.enums.ConnectOperation.HANDSHAKE;
import static ru.nchernetsov.test.sbertech.common.enums.ConnectStatus.HANDSHAKE_OK;
import static ru.nchernetsov.test.sbertech.common.enums.MethodInvokeStatus.*;
import static ru.nchernetsov.test.sbertech.common.enums.MethodInvokeStatus.NO_SUCH_METHOD;
import static ru.nchernetsov.test.sbertech.server.ReflectionHelper.callMethod;
import static ru.nchernetsov.test.sbertech.server.ReflectionHelper.instantiate;

/**
 * Основной класс сервера
 */
public class Server implements Addressee {
    private static final Logger LOG = LoggerFactory.getLogger(Server.class);
    private static final String SERVICES_DESCRIPTION_FILE = "server.properties";

    private static final int RECEIVING_THREADS_COUNT = 20;
    private static final int HANDLE_THREADS_COUNT = 5;
    private static final int MESSAGE_DELAY_MS = 117;

    private final Address address;

    // приём команд от клиентов
    private final ExecutorService receivingCommandExecutor;
    // обработка команд от клиентов
    private final ExecutorService handleCommandExecutor;

    // Карта вида <Имя сервиса - Сервис>
    private final Map<String, Object> services = new ConcurrentHashMap<>();

    // карта вида <Канал для сообщений -> соответствующий ему адрес>
    private final Map<MessageChannel, Address> connectionMap = new ConcurrentHashMap<>();

    // Адрес для клиентов, которые ещё не установили связь при помощи handshake
    private static final Address EMPTY_ADDRESS = new Address("empty");

    public static void main(String... args) {
        Integer serverPortNum;
        if (args.length > 0) {
            serverPortNum = Integer.parseInt(args[0]);
        } else {
            serverPortNum = DEFAULT_SERVER_PORT;
        }

        try {
            Server server = new Server();
            server.start(serverPortNum);
        } catch (Exception e) {
            LOG.error("Error to start Server: {}", e.getMessage());
        }
    }

    private Server() throws IOException, ClassNotFoundException, NoSuchMethodException,
        InstantiationException, IllegalAccessException, InvocationTargetException {

        receivingCommandExecutor = Executors.newFixedThreadPool(RECEIVING_THREADS_COUNT);
        handleCommandExecutor = Executors.newFixedThreadPool(HANDLE_THREADS_COUNT);

        address = SERVER_ADDRESS;

        initServices();
    }

    private void start(int serverPort) throws Exception {
        // Ждём подключения клиентов к серверу. Для подключённых клиентов создаём каналы для связи
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            LOG.info("Server started on port: " + serverSocket.getLocalPort());

            while (!receivingCommandExecutor.isShutdown()) {
                Socket client = serverSocket.accept();  // blocks

                LOG.info("Client connect: " + client);

                SocketClientChannel channel = new SocketClientChannel(client);
                channel.init();
                channel.addShutdownRegistration(() -> removeClientChannel(channel));

                connectionMap.put(channel, EMPTY_ADDRESS);

                // для каждого клиента создаём собственный обработчик
                receivingCommandExecutor.submit(() -> clientMessagesHandle(channel));
            }
        }
    }

    // Обработка сообщений от клиентов (по одному потоку на каждого клиента для приёма команд)
    private void clientMessagesHandle(MessageChannel clientChannel) {
        try {
            LOG.info("Начат цикл обработки сообщений клиента");
            while (true) {
                Message message = clientChannel.poll();
                Address clientAddress = connectionMap.get(clientChannel);
                if (message != null) {
                    if (message.isClass(ConnectOperationMessage.class)) {
                        ConnectOperationMessage connectOperationMessage = (ConnectOperationMessage) message;
                        ConnectOperation connectOperation = connectOperationMessage.getConnectOperation();
                        if (clientAddress.equals(EMPTY_ADDRESS)) {
                            if (connectOperation.equals(HANDSHAKE)) {
                                Address messageAddress = connectOperationMessage.getFrom();
                                LOG.info("Получен запрос на установление соединения от: {}. Message: {}", messageAddress, connectOperationMessage);
                                connectionMap.put(clientChannel, messageAddress);
                                ConnectAnswerMessage handshakeAnswerMessage = new ConnectAnswerMessage(
                                    SERVER_ADDRESS, messageAddress, connectOperationMessage.getUuid(), HANDSHAKE_OK);
                                clientChannel.send(handshakeAnswerMessage);
                                LOG.info("Направлен ответ об успешном установлении соединения клиенту: {}. Message: {}", messageAddress, handshakeAnswerMessage);
                            } else {
                                LOG.info("Получен не HANDSHAKE запрос от нового клиента. Message: {}", connectOperationMessage);
                            }
                        }
                    } else if (message.isClass(MethodInvokeDemandMessage.class)) {
                        MethodInvokeDemandMessage methodInvokeDemandMessage = (MethodInvokeDemandMessage) message;
                        if (getClientName(clientChannel).isPresent()) {
                            Future<MethodInvokeAnswerMessage> answerFuture = handleCommandExecutor.submit(() -> {
                                UUID toMessage = methodInvokeDemandMessage.getUuid();
                                String serviceName = methodInvokeDemandMessage.getServiceName();
                                String methodName = methodInvokeDemandMessage.getMethodName();
                                Object[] methodParams = methodInvokeDemandMessage.getMethodParams();

                                LOG.info("Запрос на выполнение метода от клиента: {}. Сервис: {}, метод: {}, параметры: {}",
                                    clientAddress, serviceName, methodName, methodParams);

                                // находим требуемый сервис
                                Object service = services.get(serviceName);

                                MethodInvokeAnswerMessage answerMessage;
                                if (service == null) {
                                    answerMessage = new MethodInvokeAnswerMessage(SERVER_ADDRESS, clientAddress, toMessage, SERVICE_NOT_EXISTS, null);
                                } else {
                                    try {
                                        Object methodResult = callMethod(service, methodName, methodParams);
                                        if (methodResult == null) {  // если вызывается метод с типом возвращаемого значения void
                                            answerMessage = new MethodInvokeAnswerMessage(SERVER_ADDRESS, clientAddress, toMessage, OK_VOID, null);
                                        } else {
                                            answerMessage = new MethodInvokeAnswerMessage(SERVER_ADDRESS, clientAddress, toMessage, OK_RESULT, methodResult);
                                        }
                                    } catch (InvocationTargetException | IllegalAccessException e) {
                                        answerMessage = new MethodInvokeAnswerMessage(SERVER_ADDRESS, clientAddress, toMessage, METHOD_INVOKE_EXCEPTION, null);
                                    } catch (NoSuchMethodException e) {
                                        answerMessage = new MethodInvokeAnswerMessage(SERVER_ADDRESS, clientAddress, toMessage, NO_SUCH_METHOD, null);
                                    }
                                }
                                return answerMessage;
                            });
                            try {
                                clientChannel.send(answerFuture.get());  // blocks here
                            } catch (ExecutionException e) {
                                LOG.error("Client command execution exception: {}", e.getMessage());
                            }
                        }
                    } else {
                        LOG.warn("От клиента получено сообщение необрабатываемог класса. Message: {}", message);
                    }
                }
                TimeUnit.MILLISECONDS.sleep(MESSAGE_DELAY_MS);
            }
        } catch (InterruptedException e) {
            LOG.error(e.getMessage());
        }
    }

    // Инициализируем сервисы
    private void initServices() throws IOException, ClassNotFoundException, InvocationTargetException,
        NoSuchMethodException, InstantiationException, IllegalAccessException {

        LOG.info("Init server services");
        Properties properties = new Properties();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(SERVICES_DESCRIPTION_FILE);
        if (inputStream != null) {
            properties.load(inputStream);
        } else {
            throw new FileNotFoundException("Property file: " + SERVICES_DESCRIPTION_FILE + " not found");
        }
        Enumeration<?> enumeration = properties.propertyNames();
        while (enumeration.hasMoreElements()) {
            String serviceName = (String) enumeration.nextElement();
            LOG.info("Init service: {}", serviceName);
            String serviceClassName = properties.getProperty(serviceName);
            Class<?> serviceClass = Class.forName(serviceClassName);
            Object serviceObject = instantiate(serviceClass);
            services.put(serviceName, serviceObject);
        }
    }

    public void stop() {
        receivingCommandExecutor.shutdownNow();
    }

    @Override
    public Address getAddress() {
        return address;
    }

    private Optional<String> getClientName(MessageChannel clientChannel) {
        if (connectionMap.containsKey(clientChannel)) {
            return Optional.of(connectionMap.get(clientChannel).getAddress());
        } else {
            return Optional.empty();
        }
    }

    private void removeClientChannel(MessageChannel clientChannel) {
        connectionMap.remove(clientChannel);
    }

}
