package ru.nchernetsov.lipt.service.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class MockDataServiceTest {

    @Qualifier("mockDataService")
    @Autowired
    private DataService dataService;

    @Test
    void cacheShouldReturnSameValueForOneInputTest() {
        GeoPoint addressCoords = dataService.getAddressGeoPoint("address #1");
        // Несколько раз снова получаем координаты для того же адреса. Они должны быть
        // взяты из кеша, и поэтому должны быть такими же
        assertThat(dataService.getAddressGeoPoint("address #1")).isEqualTo(addressCoords);
        assertThat(dataService.getAddressGeoPoint("address #1")).isEqualTo(addressCoords);
        assertThat(dataService.getAddressGeoPoint("address #1")).isEqualTo(addressCoords);
    }
}
