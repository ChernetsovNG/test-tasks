package ru.nchernetsov.test.sbertech.common.message;

import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.UUID;

/**
 * Сообщение - содержит уникальный идентификатор и адресацию (откуда -> куда)
 */
@Getter
@ToString
public abstract class Message implements Serializable {
    private final UUID uuid;        // уникальный идентификатор сообщения
    private final Address from;
    private final Address to;

    Message(Address from, Address to) {
        this.uuid = UUID.randomUUID();
        this.from = from;
        this.to = to;
    }

}
