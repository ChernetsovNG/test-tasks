package ru.nchernetsov.test.sbertech.common.message;

import lombok.Getter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.UUID;

// Сообщение - содержит адресацию (откуда -> куда)
@Getter
@ToString
public abstract class Message implements Serializable {
    public static final Logger LOG = LoggerFactory.getLogger(Message.class);
    public static final String CLASS_NAME_VARIABLE = "className";

    private final UUID uuid;        // уникальный идентификатор сообщения
    private final Address from;
    private final Address to;
    private final String className;

    protected Message(Address from, Address to, Class<?> clazz) {
        this.uuid = UUID.randomUUID();
        this.from = from;
        this.to = to;
        this.className = clazz.getName();
    }

    // Проверяем класс сообщения
    public boolean isClass(Class clazz) {
        return this.className.equals(clazz.getName());
    }

}
