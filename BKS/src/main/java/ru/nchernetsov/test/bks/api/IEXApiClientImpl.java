package ru.nchernetsov.test.bks.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.nchernetsov.test.bks.domain.StockPacket;
import ru.nchernetsov.test.bks.domain.StockPacketExt;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Service
public class IEXApiClientImpl implements ApiClient {

    private static final String BASE_URL = "https://cloud.iexapis.com/stable/stock/";
    private static final String TOKEN = "pk_7a90786dc5b54cf491dd7c55c6da2522";

    private static final WebClient CLIENT = WebClient.builder().baseUrl(BASE_URL).build();

    @Override
    public Flux<StockPacketExt> getStocksInfo(List<StockPacket> stocks) {
        if (stocks == null || stocks.isEmpty()) {
            return Flux.empty();
        }
        return Flux.fromIterable(stocks)
                .parallel()
                .runOn(Schedulers.elastic())
                .flatMap(this::getStockInfo)
                .ordered(Comparator.comparing(StockPacketExt::getSymbol));
    }

    // Обогащаем пакет акций данными из внешнего API
    private Mono<StockPacketExt> getStockInfo(final StockPacket stockPacket) {
        String symbol = stockPacket.getSymbol();
        Mono<IEXStockInfo> iexStockInfo = getIEXStockInfo(symbol);
        Mono<IEXCompanyInfo> iexCompanyInfo = getIEXCompanyInfo(symbol);
        return iexStockInfo.zipWith(iexCompanyInfo, (iexStockInfo1, iexCompanyInfo1) ->
                createStockPacketExt(stockPacket, iexStockInfo1, iexCompanyInfo1));
    }

    private Mono<IEXStockInfo> getIEXStockInfo(String symbol) {
        return CLIENT.get()
                .uri(getStockInfoUri(symbol))
                .retrieve()
                .bodyToMono(IEXStockInfo.class);
    }

    private Mono<IEXCompanyInfo> getIEXCompanyInfo(String symbol) {
        return CLIENT.get()
                .uri(getCompanyInfoUri(symbol))
                .retrieve()
                .bodyToMono(IEXCompanyInfo.class);
    }

    private StockPacketExt createStockPacketExt(StockPacket stockPacket, IEXStockInfo stock, IEXCompanyInfo company) {
        StockPacketExt stockPacketExt = new StockPacketExt();
        stockPacketExt.setStockPacket(stockPacket);
        stockPacketExt.setSymbol(stock.getSymbol());
        stockPacketExt.setLatestPrice(stock.getLatestPrice());
        stockPacketExt.setSector(company.getSector());
        stockPacketExt.setAssetVolume(stockPacket.getVolume() * stock.getLatestPrice());
        return stockPacketExt;
    }

    private Function<UriBuilder, URI> getStockInfoUri(String stock) {
        return uriBuilder -> uriBuilder
                .path(getStockInfoPath(stock))
                .queryParam("token", TOKEN)
                .build();
    }

    private Function<UriBuilder, URI> getCompanyInfoUri(String stock) {
        return uriBuilder -> uriBuilder
                .path(getCompanyInfoPath(stock))
                .queryParam("token", TOKEN).build();
    }

    private String getStockInfoPath(String stock) {
        return stock + "/quote";
    }

    private String getCompanyInfoPath(String stock) {
        return stock + "/company";
    }
}
