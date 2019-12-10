package ru.nchernetsov.test.bks.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValueAndStocksGroupBySector {

    private List<StocksGroupBySector> stocksGroupBySectors;

    /**
     * Общая стоимость портфеля акций
     */
    private Double value;
}
