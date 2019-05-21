package ru.nchernetsov.lipt.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.nchernetsov.lipt.service.DataService;
import ru.nchernetsov.lipt.service.GeoPoint;

@Slf4j
@Service
public class MockDataService implements DataService {

    @Override
    public GeoPoint getAddressCoords(String address) {
        return GeoPoint.of(30.0, 45.0);
    }
}
