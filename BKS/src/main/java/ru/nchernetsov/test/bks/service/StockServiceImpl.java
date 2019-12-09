package ru.nchernetsov.test.bks.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.nchernetsov.test.bks.api.IEXApiClient;
import ru.nchernetsov.test.bks.domain.StockAllocation;
import ru.nchernetsov.test.bks.domain.StockPacket;
import ru.nchernetsov.test.bks.domain.StocksAllocations;

import java.util.Collections;
import java.util.List;

@Service
public class StockServiceImpl implements StockService {

    private final IEXApiClient apiClient;

    public StockServiceImpl(IEXApiClient apiClient) {
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
}
