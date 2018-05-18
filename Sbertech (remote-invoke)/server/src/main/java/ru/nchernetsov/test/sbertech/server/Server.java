package ru.nchernetsov.test.sbertech.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nchernetsov.test.sbertech.common.channel.MessageChannel;
import ru.nchernetsov.test.sbertech.common.channel.SocketClientChannel;
import ru.nchernetsov.test.sbertech.common.message.*;
import ru.nchernetsov.test.sbertech.server.handler.ConnectDemandHandler;
import ru.nchernetsov.test.sbertech.server.handler.ConnectDemandHandlerImpl;
import ru.nchernetsov.test.sbertech.server.handler.DemandHandler;
import ru.nchernetsov.test.sbertech.server.handler.DemandHandlerImpl;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static ru.nchernetsov.test.sbertech.common.CommonData.DEFAULT_SERVER_PORT;
import static ru.nchernetsov.test.sbertech.common.CommonData.SERVER_ADDRESS;

public class Server implements Addressee {
    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    private static final int THREADS_COUNT = 1;
    private static final int MESSAGE_DELAY_MS = 117;

    private final Address address;

    private final ExecutorService executor;

    private final DemandHandler demandHandler;
    private final ConnectDemandHandler connectDemandHandler;

    public Server() {
        executor = Executors.newFixedThreadPool(THREADS_COUNT);

        address = SERVER_ADDRESS;

        connectDemandHandler = new ConnectDemandHandlerImpl();
        demandHandler = new DemandHandlerImpl(connectDemandHandler);
    }

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

    public void stop() {
        executor.shutdownNow();
    }

    // Обработка сообщений о соединении клиентов
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
                        } else if (message.isClass(DemandMessage.class)) {
                            demandHandler.handleDemandMessage(clientAddress, clientChannel, (DemandMessage) message);
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

    @Override
    public Address getAddress() {
        return address;
    }
}
