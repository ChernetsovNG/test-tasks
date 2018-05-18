package ru.nchernetsov.test.sbertech.server.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nchernetsov.test.sbertech.common.CommonData;
import ru.nchernetsov.test.sbertech.common.channel.MessageChannel;
import ru.nchernetsov.test.sbertech.common.enums.ConnectOperation;
import ru.nchernetsov.test.sbertech.common.enums.ConnectStatus;
import ru.nchernetsov.test.sbertech.common.message.Address;
import ru.nchernetsov.test.sbertech.common.message.ConnectAnswerMessage;
import ru.nchernetsov.test.sbertech.common.message.ConnectOperationMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Здесь выполняем процедуру handshake, запоминая соединённых клиентов
 */
public class ConnectDemandHandlerImpl implements ConnectDemandHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectDemandHandler.class);

    private final Map<MessageChannel, Address> connectionMap;  // карта вида <Канал для сообщений -> соответствующий ему адрес>

    public ConnectDemandHandlerImpl() {
        connectionMap = new HashMap<>();
    }

    @Override
    public void handleConnectDemand(Address clientAddress, MessageChannel clientChannel, ConnectOperationMessage message) {
        ConnectOperation connectOperation = message.getConnectOperation();
        if (clientAddress == null) {
            handleConnectionDemandNewClient(clientChannel, connectOperation, message);
        }
    }

    @Override
    public Map<MessageChannel, Address> getClientAddressMap() {
        return connectionMap;
    }

    private void handleConnectionDemandNewClient(MessageChannel clientChannel, ConnectOperation connectOperation, ConnectOperationMessage connectOperationMessage) {
        if (connectOperation.equals(ConnectOperation.HANDSHAKE)) {
            Address clientAddress = connectOperationMessage.getFrom();
            LOG.info("Получен запрос на установление соединения от: {}. Message: {}", clientAddress, connectOperationMessage);
            connectionMap.put(clientChannel, clientAddress);
            ConnectAnswerMessage handshakeAnswerMessage = new ConnectAnswerMessage(
                CommonData.SERVER_ADDRESS, clientAddress, connectOperationMessage.getUuid(), ConnectStatus.HANDSHAKE_OK, null);
            clientChannel.send(handshakeAnswerMessage);
            LOG.info("Направлен ответ об успешном установлении соединения клиенту: {}. Message: {}", clientAddress, handshakeAnswerMessage);
        } else {
            LOG.info("Получен не HANDSHAKE запрос от нового клиента. Message: {}", connectOperationMessage);
        }
    }

    @Override
    public Optional<String> getClientName(MessageChannel clientChannel) {
        if (connectionMap.containsKey(clientChannel)) {
            return Optional.of(connectionMap.get(clientChannel).getAddress());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void addNewClientChannel(MessageChannel clientChannel) {
        connectionMap.put(clientChannel, null);
    }

    @Override
    public void removeClientChannel(MessageChannel clientChannel) {
        connectionMap.remove(clientChannel);
    }
}
