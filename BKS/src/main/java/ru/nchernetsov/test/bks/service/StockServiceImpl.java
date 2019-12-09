package ru.nchernetsov.test.bks.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.nchernetsov.test.bks.api.ApiClient;
import ru.nchernetsov.test.bks.domain.StockAllocation;
import ru.nchernetsov.test.bks.domain.StockInfo;
import ru.nchernetsov.test.bks.domain.StockPacket;
import ru.nchernetsov.test.bks.domain.StocksAllocations;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Service
public class StockServiceImpl implements StockService {

    private final ApiClient apiClient;

    public StockServiceImpl(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Mono<StocksAllocations> calculateStocksAllocations(List<StockPacket> stocks) {
        StocksAllocations stocksAllocations = new StocksAllocations(1.0,
                Collections.singletonList(
                        new StockAllocation("test", 123.0, 0.625)));
        //apiClient.getStockInfo();
        return Mono.just(stocksAllocations);
    }

    @Override
    public Flux<StockInfo> getStocksInfo(List<String> stocks) {
        return apiClient.getStocksInfo(new HashSet<>(stocks));
    }
}
