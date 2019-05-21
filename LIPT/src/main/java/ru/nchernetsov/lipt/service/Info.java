package ru.nchernetsov.lipt.service;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import ru.nchernetsov.lipt.config.DaDataConfig;
import ru.redcom.lib.integration.api.client.dadata.DaDataClient;
import ru.redcom.lib.integration.api.client.dadata.DaDataClientFactory;
import ru.redcom.lib.integration.api.client.dadata.DaDataException;

import java.math.BigDecimal;

@Component
public class Info {

    private final DaDataClient daDataClient;

    private final DaDataConfig daDataConfig;

    public Info(final RestTemplateBuilder restTemplateBuilder, final DaDataConfig daDataConfig) {
        String apiKey = daDataConfig.getApiKey();
        String secretKey = daDataConfig.getSecretKey();
        daDataClient = DaDataClientFactory.getInstance(apiKey, secretKey, null, restTemplateBuilder);
        this.daDataConfig = daDataConfig;
    }

    public BigDecimal accountBalance() throws DaDataException {
        final BigDecimal balance = daDataClient.getProfileBalance();
        System.out.println("Balance = " + balance);
        return balance;
    }
}
