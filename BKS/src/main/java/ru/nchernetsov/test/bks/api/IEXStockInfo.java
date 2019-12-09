package ru.nchernetsov.test.bks.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IEXStockInfo {

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("latestPrice")
    private Double latestPrice;
}
