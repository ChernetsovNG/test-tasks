package ru.nchernetsov.test.sbertech.client.handler;

import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nchernetsov.test.sbertech.client.Client;
import ru.nchernetsov.test.sbertech.common.enums.ConnectStatus;
import ru.nchernetsov.test.sbertech.common.message.ConnectAnswerMessage;

import java.util.UUID;

public class ConnectAnswerHandlerImpl implements ConnectAnswerHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectAnswerHandler.class);

    @Setter
    private UUID handshakeMessageUuid;

    private final Client client;

    public ConnectAnswerHandlerImpl(Client client) {
        this.client = client;
    }

    @Override
    public void handleMessage(ConnectAnswerMessage message) {
        UUID toMessageUuid = message.getToMessage();  // по uuid проверяем, что это ответ именно на наш запрос
        if (toMessageUuid.equals(handshakeMessageUuid)) {
            if (message.getConnectStatus().equals(ConnectStatus.HANDSHAKE_OK)) {
                LOG.info("Получен ответ об установлении связи от сервера");
                if (client != null) {
                    client.unlockMessageHandle();
                }
            } else {
                LOG.info("Получен ответ, но не HANDSHAKE_OK. Message: {}", message);
            }
        } else {
            LOG.info("Пришёл ответ от сервера с UUID не в ответ на наше сообщение! Message: {}", message);
        }
    }

}
