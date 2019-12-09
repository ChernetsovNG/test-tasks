package ru.nchernetsov.test.bks.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.nchernetsov.test.bks.domain.StockInfo;

import java.net.URI;
import java.util.Comparator;
import java.util.Set;
import java.util.function.Function;

@Slf4j
@Service
public class IEXApiClientImpl implements ApiClient {

    private static final String BASE_URL = "https://cloud.iexapis.com/stable/stock/";
    private static final String TOKEN = "pk_7a90786dc5b54cf491dd7c55c6da2522";

    private static final WebClient client = WebClient.builder().baseUrl(BASE_URL).build();

    @Override
    public Flux<StockInfo> getStocksInfo(Set<String> stocks) {
        return Flux.fromIterable(stocks)
                .parallel()
                .runOn(Schedulers.elastic())
                .flatMap(this::getStockInfo)
                .ordered(Comparator.comparing(StockInfo::getSymbol));
    }

    private Mono<StockInfo> getStockInfo(String stock) {
        Mono<IEXStockInfo> iexStockInfo = getIEXStockInfo(stock);
        Mono<IEXCompanyInfo> iexCompanyInfo = getIEXCompanyInfo(stock);
        return iexStockInfo.zipWith(iexCompanyInfo, this::fromApiStockAndCompany);
    }

    private Mono<IEXStockInfo> getIEXStockInfo(String stock) {
        return client.get()
                .uri(getStockInfoUri(stock))
                .retrieve()
                .bodyToMono(IEXStockInfo.class);
    }

    private Mono<IEXCompanyInfo> getIEXCompanyInfo(String stock) {
        return client.get()
                .uri(getCompanyInfoUri(stock))
                .retrieve()
                .bodyToMono(IEXCompanyInfo.class);
    }

    private StockInfo fromApiStockAndCompany(IEXStockInfo stock, IEXCompanyInfo company) {
        StockInfo stockInfo = new StockInfo();
        stockInfo.setSymbol(stock.getSymbol());
        stockInfo.setLatestPrice(stock.getLatestPrice());
        stockInfo.setSector(company.getSector());
        return stockInfo;
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
