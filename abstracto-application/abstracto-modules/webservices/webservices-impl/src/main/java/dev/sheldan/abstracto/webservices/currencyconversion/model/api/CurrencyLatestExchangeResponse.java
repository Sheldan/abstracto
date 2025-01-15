package dev.sheldan.abstracto.webservices.currencyconversion.model.api;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CurrencyLatestExchangeResponse {
    private Map<String, Double> data;
}
