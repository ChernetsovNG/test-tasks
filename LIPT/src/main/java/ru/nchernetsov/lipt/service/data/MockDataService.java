package ru.nchernetsov.lipt.service.data;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class MockDataService implements DataService {

    @Override
    @Cacheable(cacheNames = {"address"})
    public GeoPoint getAddressCoords(String addressStr) {
        Random random = ThreadLocalRandom.current();
        double lat = -90.0 + random.nextDouble() * 180.0;
        double lon = -180.0 + random.nextDouble() * 360.0;
        return GeoPoint.of(lat, lon);
    }

    @Override
    public String getCleanAddress(String addressStr) {
        return addressStr;
    }
}
