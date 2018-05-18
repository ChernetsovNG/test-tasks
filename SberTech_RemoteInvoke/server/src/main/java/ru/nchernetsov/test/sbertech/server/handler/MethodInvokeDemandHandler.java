package ru.nchernetsov.test.sbertech.server.handler;

import ru.nchernetsov.test.sbertech.common.channel.MessageChannel;
import ru.nchernetsov.test.sbertech.common.message.Address;
import ru.nchernetsov.test.sbertech.common.message.MethodInvokeDemandMessage;

/**
 * Обработчик запросов на вызов методов от клиента
 */
public interface MethodInvokeDemandHandler {
    void handleDemandMessage(Address clientAddress, MessageChannel clientChannel, MethodInvokeDemandMessage message);
}
