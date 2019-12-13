package ru.nchernetsov.test.bks.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IEXCompanyInfo {

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("industry")
    private String industry;

    @JsonProperty("sector")
    private String sector;
}
