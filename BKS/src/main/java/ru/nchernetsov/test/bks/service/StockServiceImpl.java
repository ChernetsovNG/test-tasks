package ru.nchernetsov.test.bks.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.nchernetsov.test.bks.api.ApiClient;
import ru.nchernetsov.test.bks.api.StockPacketExtError;
import ru.nchernetsov.test.bks.domain.StockAllocation;
import ru.nchernetsov.test.bks.domain.StockPacket;
import ru.nchernetsov.test.bks.domain.StockPacketExt;
import ru.nchernetsov.test.bks.domain.StocksAllocations;

import java.util.ArrayList;
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
        validateInput(stocks);
        // 1. Получаем данные из внешнего API
        return getStocksInfo(stocks)
                // 2. Фильтруем те запросы, где от внешнего API были ошибки
                .filter(stockPacketExt -> !(stockPacketExt instanceof StockPacketExtError))
                // 3. Группируем пакеты акций по сектору
                .groupBy(StockPacketExt::getSector).flatMap(Flux::collectList)
                // 4. Считаем сумму assetValue в каждом секторе
                .map(this::getStocksGroupBySector).collectList()
                // 5. Рассчитываем общую стоимость портфеля акций
                .map(this::getValueAndStocksGroupBySector)
                // 6. Рассчитываем доли и формируем окончательный результат
                .map(this::getStocksAllocations);
    }

    private void validateInput(List<StockPacket> stocks) {
        if (stocks == null) {
            throw new IllegalArgumentException("field \"stocks\" is null");
        }
        List<StockPacket> wrongPackets = new ArrayList<>();
        for (StockPacket stock : stocks) {
            String symbol = stock.getSymbol();
            Integer volume = stock.getVolume();
            if (symbol == null || symbol.isEmpty()) {
                wrongPackets.add(stock);
            } else if (volume == null || volume < 0) {
                wrongPackets.add(stock);
            }
        }
        if (!wrongPackets.isEmpty()) {
            throw new IllegalArgumentException("There are some wrong input data = " + wrongPackets);
        }
    }

    private Flux<StockPacketExt> getStocksInfo(List<StockPacket> stocks) {
        return apiClient.getStocksInfo(stocks);
    }

    private StocksGroupBySector getStocksGroupBySector(List<StockPacketExt> stockPacketsExtBySector) {
        double sumAsset = stockPacketsExtBySector.stream()
                .mapToDouble(StockPacketExt::getAssetVolume)
                .sum();
        StocksGroupBySector stocksGroupBySector = new StocksGroupBySector();
        stocksGroupBySector.setSector(stockPacketsExtBySector.get(0).getSector());
        stocksGroupBySector.setSectorStocks(stockPacketsExtBySector);
        stocksGroupBySector.setSumAsset(sumAsset);
        return stocksGroupBySector;
    }

    private ValueAndStocksGroupBySector getValueAndStocksGroupBySector(List<StocksGroupBySector> stocksGroupBySectorsList) {
        double value = stocksGroupBySectorsList.stream()
                .mapToDouble(StocksGroupBySector::getSumAsset)
                .sum();
        ValueAndStocksGroupBySector valueAndStocksGroupBySector = new ValueAndStocksGroupBySector();
        valueAndStocksGroupBySector.setStocksGroupBySectors(stocksGroupBySectorsList);
        valueAndStocksGroupBySector.setValue(value);
        return valueAndStocksGroupBySector;
    }

    private StocksAllocations getStocksAllocations(ValueAndStocksGroupBySector valueAndStocksGroupBySector) {
        List<StocksGroupBySector> stocksGroupBySectorsList = valueAndStocksGroupBySector.getStocksGroupBySectors();
        Double value = valueAndStocksGroupBySector.getValue();
        List<StockAllocation> stockAllocations = stocksGroupBySectorsList.stream()
                .map(stocksGroupBySector -> getStockAllocation(value, stocksGroupBySector))
                .sorted(Comparator.comparing(StockAllocation::getSector))
                .collect(Collectors.toList());
        StocksAllocations stocksAllocations = new StocksAllocations();
        stocksAllocations.setValue(value);
        stocksAllocations.setAllocations(stockAllocations);
        return stocksAllocations;
    }

    private StockAllocation getStockAllocation(Double value, StocksGroupBySector stocksGroupBySector) {
        String sector = stocksGroupBySector.getSector();
        Double sumAsset = stocksGroupBySector.getSumAsset();
        Double proportion = sumAsset / value;
        StockAllocation stockAllocation = new StockAllocation();
        stockAllocation.setSector(sector);
        stockAllocation.setAssetValue(sumAsset);
        stockAllocation.setProportion(proportion);
        return stockAllocation;
    }
}
