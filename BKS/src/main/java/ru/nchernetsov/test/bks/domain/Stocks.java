package ru.nchernetsov.test.bks.domain;

import lombok.Data;

import java.util.List;

@Data
public class Stocks {

    private final List<Stock> stocks;
}
