package ru.nchernetsov.test.sbertech.common.message;

import lombok.Getter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.UUID;

/**
 * Сообщение - содержит уникальный идентификатор и адресацию (откуда -> куда)
 */
@Getter
@ToString
public abstract class Message implements Serializable {
    public static final Logger LOG = LoggerFactory.getLogger(Message.class);
    public static final String CLASS_NAME_VARIABLE = "className";

    private final UUID uuid;        // уникальный идентификатор сообщения
    private final Address from;
    private final Address to;

    Message(Address from, Address to) {
        this.uuid = UUID.randomUUID();
        this.from = from;
        this.to = to;
    }

}
