package ru.nchernetsov.test.bks.service;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.nchernetsov.test.bks.api.ApiClient;
import ru.nchernetsov.test.bks.domain.*;

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
        Flux<StockInfoWithAsset> stockInfosWithAsset = stocksInfo.zipWith(assetValues, this::getStockInfoWithAsset);

        // группируем по сектору и считаем сумму assetValue в каждом секторе
        Flux<Pair<String, Double>> sectorSumAssetFlux = stockInfosWithAsset.groupBy(StockInfoWithAsset::getSector)
                .flatMap(Flux::collectList)
                .map(stockInfoWithAssetGroup -> {
                    String sector = stockInfoWithAssetGroup.get(0).getSector();
                    double sumAsset = stockInfoWithAssetGroup.stream()
                            .map(StockInfoWithAsset::getAssetValue)
                            .mapToDouble(v -> v)
                            .sum();
                    return Pair.of(sector, sumAsset);
                });

        // Считаем пропорцию в каждом секторе
        Mono<List<StockAllocation>> stockAllocationsList = sectorSumAssetFlux.zipWith(value, (symbolSumAsset, sum) -> {
            String sector = symbolSumAsset.getLeft();
            Double sumAsset = symbolSumAsset.getRight();
            Double proportion = sumAsset / sum;
            return new StockAllocation(sector, sumAsset, proportion);
        }).collectList();

        return null;
    }

    @Override
    public Flux<StockInfo> getStocksInfo(List<String> stocks) {
        return apiClient.getStocksInfo(new HashSet<>(stocks));
    }

    private StocksAllocations getStocksAllocations(Double value, List<StockAllocation> allocations) {
        StocksAllocations stocksAllocations = new StocksAllocations();
        stocksAllocations.setValue(value);
        stocksAllocations.setAllocations(allocations);
        return stocksAllocations;
    }

    private StockInfoWithAsset getStockInfoWithAsset(StockInfo stockInfo, Double assetValue) {
        StockInfoWithAsset stockInfoWithAsset = new StockInfoWithAsset();
        stockInfoWithAsset.setSymbol(stockInfo.getSymbol());
        stockInfoWithAsset.setSector(stockInfo.getSector());
        stockInfoWithAsset.setLatestPrice(stockInfo.getLatestPrice());
        stockInfoWithAsset.setAssetValue(assetValue);
        return stockInfoWithAsset;
    }
}
