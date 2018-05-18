package ru.nchernetsov.test.sbertech.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nchernetsov.test.sbertech.common.channel.MessageChannel;
import ru.nchernetsov.test.sbertech.common.channel.SocketClientChannel;
import ru.nchernetsov.test.sbertech.common.message.*;
import ru.nchernetsov.test.sbertech.server.handler.ConnectDemandHandler;
import ru.nchernetsov.test.sbertech.server.handler.ConnectDemandHandlerImpl;
import ru.nchernetsov.test.sbertech.server.handler.MethodInvokeDemandHandler;
import ru.nchernetsov.test.sbertech.server.handler.MethodInvokeDemandHandlerImpl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static ru.nchernetsov.test.sbertech.common.CommonData.DEFAULT_SERVER_PORT;
import static ru.nchernetsov.test.sbertech.common.CommonData.SERVER_ADDRESS;

/**
 * Основной класс сервера
 */
public class Server implements Addressee {
    private static final Logger LOG = LoggerFactory.getLogger(Server.class);
    private static final String SERVICES_DESCRIPTION_FILE = "server.properties";

    private static final int THREADS_COUNT = 2;
    private static final int MESSAGE_DELAY_MS = 117;

    private final Address address;

    private final ExecutorService executor;

    private final MethodInvokeDemandHandler methodInvokeDemandHandler;
    private final ConnectDemandHandler connectDemandHandler;

    // Карта вида <Имя сервиса - Сервис>
    private Map<String, Object> services = new ConcurrentHashMap<>();

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
            e.printStackTrace();
        }
    }

    public Server() throws IOException, ClassNotFoundException, NoSuchMethodException,
        InstantiationException, IllegalAccessException, InvocationTargetException {

        executor = Executors.newFixedThreadPool(THREADS_COUNT);

        address = SERVER_ADDRESS;

        initServices();

        connectDemandHandler = new ConnectDemandHandlerImpl();
        methodInvokeDemandHandler = new MethodInvokeDemandHandlerImpl(connectDemandHandler, services);
    }

    public void start(int serverPort) throws Exception {
        executor.submit(this::clientMessageHandle);

        // Ждём подключения клиентов к серверу. Для подключённых клиентов создаём каналы для связи
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            LOG.info("Server started on port: " + serverSocket.getLocalPort());

            while (!executor.isShutdown()) {
                Socket client = serverSocket.accept();  // blocks

                LOG.info("Client connect: " + client);

                SocketClientChannel channel = new SocketClientChannel(client);
                channel.init();
                channel.addShutdownRegistration(() -> connectDemandHandler.removeClientChannel(channel));

                connectDemandHandler.addNewClientChannel(channel);
            }
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
            Object serviceObject = ReflectionHelper.instantiate(serviceClass);
            services.put(serviceName, serviceObject);
        }
    }

    // Обработка сообщений от клиентов
    private void clientMessageHandle() {
        try {
            LOG.info("Начат цикл обработки соединений клиентов");
            while (true) {
                for (Map.Entry<MessageChannel, Address> client : connectDemandHandler.getClientAddressMap().entrySet()) {
                    MessageChannel clientChannel = client.getKey();
                    Address clientAddress = client.getValue();
                    Message message = clientChannel.poll();
                    if (message != null) {
                        if (message.isClass(ConnectOperationMessage.class)) {
                            connectDemandHandler.handleConnectDemand(clientAddress, clientChannel, (ConnectOperationMessage) message);
                        } else if (message.isClass(MethodInvokeDemandMessage.class)) {
                            methodInvokeDemandHandler.handleDemandMessage(clientAddress, clientChannel, (MethodInvokeDemandMessage) message);
                        } else {
                            LOG.warn("От клиента получено сообщение необрабатываемог класса. Message: {}", message);
                        }
                    }
                }
                TimeUnit.MILLISECONDS.sleep(MESSAGE_DELAY_MS);
            }
        } catch (InterruptedException e) {
            LOG.error(e.getMessage());
        }
    }

    public void stop() {
        executor.shutdownNow();
    }

    @Override
    public Address getAddress() {
        return address;
    }

}
