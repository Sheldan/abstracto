package dev.sheldan.abstracto.webservices.currencyconversion.service;

import dev.sheldan.abstracto.webservices.currencyconversion.model.Currency;
import java.util.List;
import java.util.Map;

public interface CurrencyConversionApiService {
    List<Currency> getSupportedCurrencies();
    Currency getCurrencyForString(String input);
    Double convertCurrency(String sourceCurrency, String targetCurrency, Double amount);
    Map<String, Double> getExchangeRates(Currency sourceCurrency);
    Double convertCurrency(Currency sourceCurrency, Currency targetCurrency, Double amount);
}
