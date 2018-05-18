package ru.nchernetsov.test.sbertech.server.handler;

import ru.nchernetsov.test.sbertech.common.channel.MessageChannel;
import ru.nchernetsov.test.sbertech.common.message.Address;
import ru.nchernetsov.test.sbertech.common.message.DemandMessage;

import java.util.Map;

/**
 * Обработчик запросов от клиента
 */
public interface DemandHandler {
    void handleDemandMessage(Address clientAddress, MessageChannel clientChannel, DemandMessage message);
}
