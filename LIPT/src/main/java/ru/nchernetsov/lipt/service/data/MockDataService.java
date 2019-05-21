package ru.nchernetsov.lipt.service.data;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MockDataService implements DataService {

    @Override
    public GeoPoint getAddressCoords(String address) {
        return GeoPoint.of(30.0, 45.0);
    }
}
