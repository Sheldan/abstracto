package dev.sheldan.abstracto.webservices.currencyconversion.model.api;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CurrencyListCurrency {
    private String symbol;
    private String name;
    @SerializedName("symbol_native")
    private String symbolNative;
    @SerializedName("decimal_digits")
    private String decimalDigits;
    private String rounding;
    private String code;
    @SerializedName("name_plural")
    private String namePlural;
}
