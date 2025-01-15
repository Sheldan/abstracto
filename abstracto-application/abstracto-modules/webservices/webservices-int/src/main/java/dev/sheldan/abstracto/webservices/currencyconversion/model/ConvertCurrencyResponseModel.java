package dev.sheldan.abstracto.webservices.currencyconversion.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ConvertCurrencyResponseModel {
    private Currency sourceCurrency;
    private Currency targetCurrency;
    private Double sourceValue;
    private Double targetValue;
}
