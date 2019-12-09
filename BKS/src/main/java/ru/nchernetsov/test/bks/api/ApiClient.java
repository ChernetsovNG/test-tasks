package ru.nchernetsov.test.bks.api;

import reactor.core.publisher.Flux;
import ru.nchernetsov.test.bks.domain.StockInfo;

import java.util.Set;

public interface ApiClient {

    /**
     * Получить для списка акций информацию о них
     *
     * @param stocks список названий акций
     */
    Flux<StockInfo> getStocksInfo(Set<String> stocks);
}
