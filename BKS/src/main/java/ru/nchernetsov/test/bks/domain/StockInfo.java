package ru.nchernetsov.test.bks.domain;

import lombok.Data;

@Data
public class StockInfo {

    private String symbol;

    /**
     * Текущая котировка акции
     */
    private Double latestPrice;

    /**
     * Сектор, в котором работает компания, выпускающая данные акции
     */
    private String sector;
}
