package ru.nchernetsov.lipt.service.data;

public interface DataService {

    /**
     * Получить географические координаты заданного адреса (например, "спб невский 17")
     *
     * @param addressStr адрес в достаточно произвольной форме
     * @return географические координаты
     */
    GeoPoint getAddressGeoPoint(String addressStr);
}
