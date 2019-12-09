package ru.nchernetsov.test.bks.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.nchernetsov.test.bks.api.ApiClient;
import ru.nchernetsov.test.bks.domain.StockInfo;
import ru.nchernetsov.test.bks.domain.StockPacket;
import ru.nchernetsov.test.bks.domain.StocksAllocations;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StockServiceImpl implements StockService {

    private final ApiClient apiClient;

    public StockServiceImpl(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Mono<StocksAllocations> calculateStocksAllocations(List<StockPacket> stocks) {
        // карта вида (акция - объём портфеля)
        Map<String, Integer> symbolVolumeMap = stocks.stream()
                .collect(Collectors.toMap(StockPacket::getSymbol, StockPacket::getVolume));

        List<String> symbols = stocks.stream()
                .map(StockPacket::getSymbol)
                .collect(Collectors.toList());

        // 1. Для акций находим актуальную стоимость и сектор
        Flux<StockInfo> stocksInfo = getStocksInfo(symbols);

        // 2. Рассчитать для каждой акции ее текущую стоимость (assetValue) в составе портфеля по формуле volume * latestPrice
        Flux<Double> assetValues = stocksInfo
                .map(stockInfo -> {
                    String symbol = stockInfo.getSymbol();
                    Integer volume = symbolVolumeMap.get(symbol);
                    Double latestPrice = stockInfo.getLatestPrice();
                    return volume * latestPrice;
                });

        // 3. Посчитать суммарную стоимость портфеля (value), выполнив sum(assetValue) по всем акциям
        Mono<Double> value = assetValues.reduce(0.0, Double::sum);

        // 4. Посчитать процентное соотношение стоимости акций для каждого сектора (proportion),
        // используя формулу: sum(assetValue) in sector / value


        return null;
    }

    @Override
    public Flux<StockInfo> getStocksInfo(List<String> stocks) {
        return apiClient.getStocksInfo(new HashSet<>(stocks));
    }
}
