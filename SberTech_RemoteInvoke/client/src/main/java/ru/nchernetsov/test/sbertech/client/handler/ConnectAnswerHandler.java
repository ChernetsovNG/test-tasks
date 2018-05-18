package ru.nchernetsov.test.sbertech.client.handler;

import ru.nchernetsov.test.sbertech.common.message.ConnectAnswerMessage;

import java.util.UUID;

/**
 * Обработка ответов о соединении с серверм
 */
public interface ConnectAnswerHandler {
    void handleMessage(ConnectAnswerMessage message);

    void setHandshakeMessageUuid(UUID handshakeMessageUuid);
}
