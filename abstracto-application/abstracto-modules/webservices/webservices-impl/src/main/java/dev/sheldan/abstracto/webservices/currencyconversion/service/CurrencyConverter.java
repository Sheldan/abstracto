package dev.sheldan.abstracto.webservices.currencyconversion.service;

import dev.sheldan.abstracto.webservices.currencyconversion.model.Currency;
import dev.sheldan.abstracto.webservices.currencyconversion.model.api.CurrencyListCurrency;
import org.springframework.stereotype.Component;

@Component
public class CurrencyConverter {
    public Currency fromResponseObject(CurrencyListCurrency currencyListCurrency) {
        return Currency
            .builder()
            .code(currencyListCurrency.getCode())
            .name(currencyListCurrency.getName())
            .symbol(currencyListCurrency.getSymbol())
            .symbolNative(currencyListCurrency.getSymbolNative())
            .build();
    }
}
