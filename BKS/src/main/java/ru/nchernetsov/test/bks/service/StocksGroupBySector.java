package ru.nchernetsov.test.bks.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.nchernetsov.test.bks.domain.StockPacketExt;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StocksGroupBySector {

    private String sector;

    private List<StockPacketExt> sectorStocks;

    /**
     * Суммарная стоимость акций для сектора
     */
    private Double sumAsset;
}
