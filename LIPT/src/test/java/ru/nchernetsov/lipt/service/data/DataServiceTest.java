package ru.nchernetsov.lipt.service.data;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class DataServiceTest {

    @MockBean
    private DataService dataService;

    @BeforeEach
    void setUp() {
        given(dataService.getAddressCoords(eq("Москва")))
                .willReturn(GeoPoint.of(10.0, 15.0));

        given(dataService.getAddressCoords(eq("Санкт-Петербург")))
                .willReturn(GeoPoint.of(30.0, 35.0));

        given(dataService.getAddressCoords(eq("Киев")))
                .willReturn(GeoPoint.of(40.0, 45.0));
    }

    @Test
    void getAddressCoordsTest1() {
        GeoPoint addressCoords = dataService.getAddressCoords("Москва");
        assertThat(addressCoords.getLat()).isEqualTo(10.0, Offset.offset(1e-6));
        assertThat(addressCoords.getLon()).isEqualTo(15.0, Offset.offset(1e-6));
    }
}
