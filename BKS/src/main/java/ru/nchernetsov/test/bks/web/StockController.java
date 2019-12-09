package ru.nchernetsov.test.bks.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.nchernetsov.test.bks.domain.StockInfo;
import ru.nchernetsov.test.bks.domain.Stocks;
import ru.nchernetsov.test.bks.domain.StocksAllocations;
import ru.nchernetsov.test.bks.service.StockService;

import java.util.List;

@RestController
@Slf4j
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping(value = "/stocks/allocations", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Mono<StocksAllocations>> calcStocksAllocations(@RequestBody Stocks stocks) {
        log.debug("Invoke /stocks/allocations: stocks = {}", stocks);
        return ResponseEntity.ok(stockService.calculateStocksAllocations(stocks.getStocks()));
    }

    @GetMapping(value = "/stocks/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Flux<StockInfo>> getStocksInfo(@RequestParam("stock") List<String> stocks) {
        return ResponseEntity.ok(stockService.getStocksInfo(stocks));
    }
}
