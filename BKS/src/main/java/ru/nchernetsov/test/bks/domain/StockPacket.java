package ru.nchernetsov.test.bks.domain;

import lombok.Data;

@Data
public class StockPacket {

    /**
     * Символ акции
     */
    private String symbol;

    /**
     * Количество акций
     */
    private Integer volume;
}
