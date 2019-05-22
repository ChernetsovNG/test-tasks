package ru.nchernetsov.lipt.service.data;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.nchernetsov.lipt.config.DaDataConfig;
import ru.redcom.lib.integration.api.client.dadata.DaDataClient;
import ru.redcom.lib.integration.api.client.dadata.DaDataClientFactory;
import ru.redcom.lib.integration.api.client.dadata.dto.Address;

@Slf4j
@Service
public class DaDataService implements DataService {

    private final DaDataClient daDataClient;

    public DaDataService(RestTemplateBuilder restTemplateBuilder, DaDataConfig daDataConfig) {
        String apiKey = daDataConfig.getApiKey();
        String secretKey = daDataConfig.getSecretKey();
        daDataClient = DaDataClientFactory.getInstance(apiKey, secretKey, null, restTemplateBuilder);
    }

    @Override
    public GeoPoint getAddressCoords(String addressStr) {
        Address address = getAddress(addressStr);

        Double geoLat = address.getGeoLat();
        Double geoLon = address.getGeoLon();

        return GeoPoint.of(geoLat, geoLon);
    }

    @Cacheable(cacheNames = {"address"})
    public Address getAddress(String addressStr) {
        return daDataClient.cleanAddress(addressStr);
    }
}
