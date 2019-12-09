package ru.nchernetsov.test.bks.domain;

import lombok.Data;

import java.util.List;

@Data
public class StocksAllocations {

    private final Double value;

    private final List<StockAllocation> allocations;
}
