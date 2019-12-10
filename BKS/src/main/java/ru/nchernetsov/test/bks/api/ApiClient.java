package ru.nchernetsov.test.bks.api;

import reactor.core.publisher.Flux;
import ru.nchernetsov.test.bks.domain.StockPacket;
import ru.nchernetsov.test.bks.domain.StockPacketExt;

import java.util.List;

public interface ApiClient {

    /**
     * Получить для списка акций информацию о них из внешнего API
     *
     * @param stocks список пакетов акций
     */
    Flux<StockPacketExt> getStocksInfo(List<StockPacket> stocks);
}
