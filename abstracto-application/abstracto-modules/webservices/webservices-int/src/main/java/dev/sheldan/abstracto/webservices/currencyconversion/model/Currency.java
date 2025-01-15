package dev.sheldan.abstracto.webservices.currencyconversion.model;

import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@EqualsAndHashCode
@ToString
public class Currency {
    private String symbol;
    private String symbolNative;
    private String name;
    private String code;
    private Map<String, Double> exchangeRates;
}
