package ru.nchernetsov.test.sbertech.common.channel;

import ru.nchernetsov.test.sbertech.common.message.Message;

import java.io.IOException;

/**
 * Канал для передачи и получения сообщений
 */
public interface MessageChannel {
    void send(Message message);

    Message poll();

    Message take() throws InterruptedException;

    void close() throws IOException;
}
