package ru.nchernetsov.test.bks.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
