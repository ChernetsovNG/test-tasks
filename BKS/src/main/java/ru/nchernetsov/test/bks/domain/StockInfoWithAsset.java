package ru.nchernetsov.test.bks.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class StockInfoWithAsset extends StockInfo {

    /**
     * Стоимость пакета акций
     */
    private Double assetValue;
}
