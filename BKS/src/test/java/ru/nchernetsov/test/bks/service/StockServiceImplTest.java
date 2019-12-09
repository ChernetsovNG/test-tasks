package ru.nchernetsov.test.bks.service;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.nchernetsov.test.bks.api.ApiClient;
import ru.nchernetsov.test.bks.domain.StockAllocation;
import ru.nchernetsov.test.bks.domain.StockInfo;
import ru.nchernetsov.test.bks.domain.StockPacket;
import ru.nchernetsov.test.bks.domain.StocksAllocations;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StockServiceImplTest {

    @Mock
    private ApiClient apiClient;

    private StockService stockService;

    @BeforeEach
    public void setUp() {
        given(apiClient.getStocksInfo(new HashSet<>(Arrays.asList(
                "AAPL", "HOG", "MDSO", "IDRA", "MRSN")))).willReturn(
                Flux.fromIterable(Arrays.asList(
                        new StockInfo("AAPL", 265.49, "Electronic Technology"),
                        new StockInfo("HOG", 37.025, "Consumer Durables"),
                        new StockInfo("MDSO", 92.22, "Technology Services"),
                        new StockInfo("IDRA", 1.81, "Health Technology"),
                        new StockInfo("MRSN", 4.29, "Health Technology"))));

        stockService = new StockServiceImpl(apiClient);
    }

    @Test
    void calculateStocksAllocations() {
        List<StockPacket> stocks = Arrays.asList(
                new StockPacket("AAPL", 50),
                new StockPacket("HOG", 10),
                new StockPacket("MDSO", 1),
                new StockPacket("IDRA", 1),
                new StockPacket("MRSN", 1));

        Mono<StocksAllocations> stocksAllocationsMono = stockService.calculateStocksAllocations(stocks);
        StocksAllocations stocksAllocations = stocksAllocationsMono.block();

        double sumValue = 50 * 265.49 + 10 * 37.025 + 1 * 92.22 + 1 * 1.81 + 1 * 4.29;

        assertThat(stocksAllocations).isNotNull();
        assertThat(stocksAllocations.getValue()).isEqualTo(sumValue, Offset.offset(1e-6));
        List<StockAllocation> allocations = stocksAllocations.getAllocations();
        assertThat(allocations).isNotNull();
        assertThat(allocations).hasSize(4);
        assertThat(allocations.get(0)).isEqualTo(new StockAllocation("Consumer Durables", 10 * 37.025, 10 * 37.025 / sumValue));
        assertThat(allocations.get(1)).isEqualTo(new StockAllocation("Electronic Technology", 50 * 265.49, 50 * 265.49 / sumValue));
        assertThat(allocations.get(2)).isEqualTo(new StockAllocation("Health Technology", 1 * 1.81 + 1 * 4.29, (1 * 1.81 + 1 * 4.29) / sumValue));
        assertThat(allocations.get(3)).isEqualTo(new StockAllocation("Technology Services", 1 * 92.22, 1 * 92.22 / sumValue));
    }
}
