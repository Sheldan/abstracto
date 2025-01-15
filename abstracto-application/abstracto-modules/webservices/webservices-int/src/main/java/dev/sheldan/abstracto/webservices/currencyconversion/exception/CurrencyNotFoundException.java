package dev.sheldan.abstracto.webservices.currencyconversion.exception;

import dev.sheldan.abstracto.core.command.exception.AbstractoTemplatedException;

public class CurrencyNotFoundException extends AbstractoTemplatedException {
    public CurrencyNotFoundException() {
        super("Currency not found", "currency_conversion_currency_not_found_exception");
    }
}
