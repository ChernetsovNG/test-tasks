package ru.nchernetsov.test.sbertech.common.message;

import lombok.Data;

import java.io.Serializable;

/**
 * Адрес для указания отправителя и получателя сообщений
 */
@Data
public class Address implements Serializable {
    private final String address;

    public Address(String address) {
        this.address = address;
    }
}
