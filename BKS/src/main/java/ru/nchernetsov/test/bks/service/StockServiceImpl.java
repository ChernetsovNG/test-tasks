package ru.nchernetsov.test.bks.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.nchernetsov.test.bks.api.ApiClient;
import ru.nchernetsov.test.bks.domain.StockAllocation;
import ru.nchernetsov.test.bks.domain.StockPacket;
import ru.nchernetsov.test.bks.domain.StockPacketExt;
import ru.nchernetsov.test.bks.domain.StocksAllocations;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StockServiceImpl implements StockService {

    private final ApiClient apiClient;

    public StockServiceImpl(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Mono<StocksAllocations> calculateStocksAllocations(List<StockPacket> stocks) {
        // 1. Получаем данные из внешнего API
        Flux<StockPacketExt> stockPacketsExt = getStocksInfo(stocks);

        // 2. Группируем по сектору и считаем сумму assetValue в каждом секторе
        Flux<List<StockPacketExt>> stockPacketsExtGroupBySector = stockPacketsExt
                .groupBy(StockPacketExt::getSector)
                .flatMap(Flux::collectList);

        Mono<List<StocksGroupBySector>> stocksGroupBySectors = stockPacketsExtGroupBySector
                .map(stockPacketsExtBySector -> {
                    double sumAsset = stockPacketsExtBySector.stream()
                            .mapToDouble(StockPacketExt::getAssetVolume)
                            .sum();
                    StocksGroupBySector stocksGroupBySector = new StocksGroupBySector();
                    stocksGroupBySector.setSector(stockPacketsExtBySector.get(0).getSector());
                    stocksGroupBySector.setSectorStocks(stockPacketsExtBySector);
                    stocksGroupBySector.setSumAsset(sumAsset);
                    return stocksGroupBySector;
                })
                .collectList();

        // 3. Рассчитываем общую стоимость портфеля акций
        Mono<ValueAndStocksGroupBySector> valueAndStocksGroupBySectorMono = stocksGroupBySectors
                .map(stocksGroupBySectorsList -> {
                    double value = stocksGroupBySectorsList.stream()
                            .mapToDouble(StocksGroupBySector::getSumAsset)
                            .sum();
                    ValueAndStocksGroupBySector valueAndStocksGroupBySector = new ValueAndStocksGroupBySector();
                    valueAndStocksGroupBySector.setStocksGroupBySectors(stocksGroupBySectorsList);
                    valueAndStocksGroupBySector.setValue(value);
                    return valueAndStocksGroupBySector;
                });

        // 4. Рассчитываем доли и формируем окончательный результат
        return valueAndStocksGroupBySectorMono
                .map(valueAndStocksGroupBySector -> {
                    List<StocksGroupBySector> stocksGroupBySectorsList = valueAndStocksGroupBySector.getStocksGroupBySectors();
                    Double value = valueAndStocksGroupBySector.getValue();
                    List<StockAllocation> stockAllocations = stocksGroupBySectorsList.stream()
                            .map(stocksGroupBySector -> {
                                String sector = stocksGroupBySector.getSector();
                                Double sumAsset = stocksGroupBySector.getSumAsset();
                                Double proportion = sumAsset / value;
                                StockAllocation stockAllocation = new StockAllocation();
                                stockAllocation.setSector(sector);
                                stockAllocation.setAssetValue(sumAsset);
                                stockAllocation.setProportion(proportion);
                                return stockAllocation;
                            })
                            .sorted(Comparator.comparing(StockAllocation::getSector))
                            .collect(Collectors.toList());
                    StocksAllocations stocksAllocations = new StocksAllocations();
                    stocksAllocations.setValue(value);
                    stocksAllocations.setAllocations(stockAllocations);
                    return stocksAllocations;
                });
    }

    @Override
    public Flux<StockPacketExt> getStocksInfo(List<StockPacket> stocks) {
        return apiClient.getStocksInfo(stocks);
    }
}
