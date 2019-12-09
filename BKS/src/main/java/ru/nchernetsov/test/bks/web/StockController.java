package ru.nchernetsov.test.bks.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.nchernetsov.test.bks.domain.Stocks;
import ru.nchernetsov.test.bks.domain.StocksAllocations;

@RestController("/stocks")
public class StockController {

    @PostMapping("/allocations")
    public Mono<StocksAllocations> calcStocksAllocation(@RequestBody Stocks stocks) {
        return Mono.just(null);
    }
}
