package ru.nchernetsov.test.bks.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IEXCompanyInfo {

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("industry")
    private String industry;

    @JsonProperty("sector")
    private String sector;
}
