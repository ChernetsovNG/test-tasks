package ru.nchernetsov.lipt.service;

public interface DataService {

    /**
     * Получить географические координаты заданного адреса (например, "спб невский 17")
     *
     * @param address адрес в достаточно произвольной форме
     * @return географические координаты
     */
    GeoPoint getAddressCoords(String address);
}
