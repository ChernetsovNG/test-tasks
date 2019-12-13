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
import ru.nchernetsov.test.bks.domain.StockPacket;
import ru.nchernetsov.test.bks.domain.StockPacketExt;
import ru.nchernetsov.test.bks.domain.StocksAllocations;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StockServiceImplTest {

    @Mock
    private ApiClient apiClient;

    private StockService stockService;

    @BeforeEach
    public void setUp() {
        stockService = new StockServiceImpl(apiClient);
    }

    @Test
    void wrongInputNullStocksListShouldThrowIllegalArgumentException() {
        List<StockPacket> stocks = null;
        assertThrows(IllegalArgumentException.class, () -> stockService.calculateStocksAllocations(stocks));
    }

    @Test
    void wrongInputNullSymbolShouldThrowIllegalArgumentException() {
        List<StockPacket> stocks = Arrays.asList(
                new StockPacket(null, 1),
                new StockPacket("AAPL", 50));
        assertThrows(IllegalArgumentException.class, () -> stockService.calculateStocksAllocations(stocks));
    }

    @Test
    void wrongInputEmptySymbolShouldThrowIllegalArgumentException() {
        List<StockPacket> stocks = Arrays.asList(
                new StockPacket("", 1),
                new StockPacket("AAPL", 50));
        assertThrows(IllegalArgumentException.class, () -> stockService.calculateStocksAllocations(stocks));
    }

    @Test
    void wrongInputNullVolumeShouldThrowIllegalArgumentException() {
        List<StockPacket> stocks = Arrays.asList(
                new StockPacket("HOG", null),
                new StockPacket("AAPL", 50));
        assertThrows(IllegalArgumentException.class, () -> stockService.calculateStocksAllocations(stocks));
    }

    @Test
    void wrongInputNegativeVolumeShouldThrowIllegalArgumentException() {
        List<StockPacket> stocks = Arrays.asList(
                new StockPacket("HOG", -10),
                new StockPacket("AAPL", 50));
        assertThrows(IllegalArgumentException.class, () -> stockService.calculateStocksAllocations(stocks));
    }

    @Test
    void calculateStocksAllocations() {
        StockPacket stockPacket1 = new StockPacket("AAPL", 50);
        StockPacket stockPacket2 = new StockPacket("HOG", 10);
        StockPacket stockPacket3 = new StockPacket("MDSO", 1);
        StockPacket stockPacket4 = new StockPacket("IDRA", 1);
        StockPacket stockPacket5 = new StockPacket("MRSN", 1);

        List<StockPacket> stockPackets = Arrays.asList(stockPacket1, stockPacket2, stockPacket3, stockPacket4, stockPacket5);

        given(apiClient.getStocksInfo(stockPackets)).willReturn(
                Flux.fromIterable(Arrays.asList(
                        new StockPacketExt(stockPacket1, "AAPL", 265.49, "Electronic Technology", 50 * 265.49),
                        new StockPacketExt(stockPacket2, "HOG", 37.025, "Consumer Durables", 10 * 37.025),
                        new StockPacketExt(stockPacket3, "MDSO", 92.22, "Technology Services", 1 * 92.22),
                        new StockPacketExt(stockPacket4, "IDRA", 1.81, "Health Technology", 1 * 1.81),
                        new StockPacketExt(stockPacket5, "MRSN", 4.29, "Health Technology", 1 * 4.29))));

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

        assertThat(allocations.get(0).getSector()).isEqualTo("Consumer Durables");
        assertThat(allocations.get(0).getAssetValue()).isEqualTo(10 * 37.025, Offset.offset(1e-6));
        assertThat(allocations.get(0).getProportion()).isEqualTo(10 * 37.025 / sumValue, Offset.offset(1e-6));

        assertThat(allocations.get(1).getSector()).isEqualTo("Electronic Technology");
        assertThat(allocations.get(1).getAssetValue()).isEqualTo(50 * 265.49, Offset.offset(1e-6));
        assertThat(allocations.get(1).getProportion()).isEqualTo(50 * 265.49 / sumValue, Offset.offset(1e-6));

        assertThat(allocations.get(2).getSector()).isEqualTo("Health Technology");
        assertThat(allocations.get(2).getAssetValue()).isEqualTo(1 * 1.81 + 1 * 4.29, Offset.offset(1e-6));
        assertThat(allocations.get(2).getProportion()).isEqualTo((1 * 1.81 + 1 * 4.29) / sumValue, Offset.offset(1e-6));

        assertThat(allocations.get(3).getSector()).isEqualTo("Technology Services");
        assertThat(allocations.get(3).getAssetValue()).isEqualTo(1 * 92.22, Offset.offset(1e-6));
        assertThat(allocations.get(3).getProportion()).isEqualTo(1 * 92.22 / sumValue, Offset.offset(1e-6));
    }
}
