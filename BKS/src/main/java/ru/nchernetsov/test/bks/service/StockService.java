package ru.nchernetsov.test.bks.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.nchernetsov.test.bks.domain.StockPacketExt;
import ru.nchernetsov.test.bks.domain.StockPacket;
import ru.nchernetsov.test.bks.domain.StocksAllocations;

import java.util.List;

public interface StockService {

    /**
     * Рассчитать распределение набора акций по секторам
     *
     * @param stocks набор акций
     */
    Mono<StocksAllocations> calculateStocksAllocations(List<StockPacket> stocks);

    Flux<StockPacketExt> getStocksInfo(List<StockPacket> stocks);
}
