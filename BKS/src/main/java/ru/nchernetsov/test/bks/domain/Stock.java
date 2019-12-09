package ru.nchernetsov.test.bks.domain;

import lombok.Data;

@Data
public class Stock {

    /**
     * Символ акции
     */
    private final String symbol;

    /**
     * Количество акций
     */
    private final Integer volume;
}
