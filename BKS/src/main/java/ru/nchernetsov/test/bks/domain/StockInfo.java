package ru.nchernetsov.test.bks.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
