package ru.nchernetsov.test.bks.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockAllocation {

    /**
     * Сектор, в котором работает компания
     */
    private String sector;

    /**
     * Текущая стоимость акции в составе портфеля
     */
    private Double assertValue;

    /**
     * Процентное соотношение стоимости акций для сектора
     */
    private Double proportion;
}
