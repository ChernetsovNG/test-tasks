package ru.nchernetsov.test.bks.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class IEXApiClient {

    private static final String TOKEN = "pk_7a90786dc5b54cf491dd7c55c6da2522";

    private static final String BASE_URL = "https://cloud.iexapis.com/stable/stock/";

    private static final String URL_TEMPLATE = "https://cloud.iexapis.com/stable/stock/%s/quote?token=%s";

    public void getStockInfo(String stockName) {
        String path = stockName + "/quote";
        WebClient webClient = WebClient.builder().baseUrl(BASE_URL).build();
        WebClient.ResponseSpec response = webClient
                .get()
                .uri(uriBuilder -> uriBuilder.path(path).queryParam("token", TOKEN).build())
                .retrieve();
        Mono<IEXStockInfo> objectMono = response.bodyToMono(IEXStockInfo.class);
        IEXStockInfo block = objectMono.block();
        log.debug("Response stock = {}", block);
    }

    public void getCompanyInfo(String stockName) {
        String path = stockName + "/company";
        WebClient webClient = WebClient.builder().baseUrl(BASE_URL).build();
        WebClient.ResponseSpec response = webClient
                .get()
                .uri(uriBuilder -> uriBuilder.path(path).queryParam("token", TOKEN).build())
                .retrieve();
        Mono<IEXCompanyInfo> objectMono = response.bodyToMono(IEXCompanyInfo.class);
        IEXCompanyInfo block = objectMono.block();
        log.debug("Response company = {}", block);
    }

    public static void main(String[] args) {
        String stockName = "AAPL";
        IEXApiClient iexApiClient = new IEXApiClient();
        iexApiClient.getStockInfo(stockName);
        //iexApiClient.getCompanyInfo(stockName);
    }
}
