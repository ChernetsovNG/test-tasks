package ru.nchernetsov.test.bks.domain;

import lombok.Data;

@Data
public class StockAllocation {

    /**
     * Сектор, в котором работает компания
     */
    private final String sector;

    /**
     * Текущая стоимость акции в составе портфеля
     */
    private final Double assertValue;

    /**
     * Процентное соотношение стоимости акций для сектора
     */
    private final Double proportion;
}
