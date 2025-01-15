package dev.sheldan.abstracto.webservices.currencyconversion.service;

import com.google.gson.Gson;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.webservices.currencyconversion.exception.CurrencyNotFoundException;
import dev.sheldan.abstracto.webservices.currencyconversion.model.Currency;
import dev.sheldan.abstracto.webservices.currencyconversion.model.api.CurrencyLatestExchangeResponse;
import dev.sheldan.abstracto.webservices.currencyconversion.model.api.CurrencyListResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CurrencyConversionApiServiceBean implements CurrencyConversionApiService {

    @Autowired
    private OkHttpClient okHttpClient;

    @Value("${abstracto.feature.webservices.currencyConversion.currencyURL}")
    private String currencyUrl;

    @Value("${abstracto.feature.webservices.currencyConversion.conversionURL}")
    private String conversionUrl;

    @Value("${abstracto.feature.webservices.currencyConversion.apiKey}")
    private String apiKey;

    @Autowired
    private Gson gson;

    @Autowired
    private CurrencyConversionApiService self;

    @Autowired
    private CurrencyConverter currencyConverter;

    @Override
    @Cacheable(value = "currency-cache")
    public List<Currency> getSupportedCurrencies() {
        String formattedUrl = currencyUrl;
        Request request = new Request.Builder()
            .url(formattedUrl)
            .header("apikey", apiKey)
            .get()
            .build();
        Response response = null;
        log.info("Loading available currencies.");
        try {
            response = okHttpClient.newCall(request).execute();
            CurrencyListResponse currencyListResponse = gson.fromJson(response.body().string(), CurrencyListResponse.class);
            List<Currency> currencies = new ArrayList<>();
            currencyListResponse.getData().forEach((s, currencyListCurrency) -> currencies.add(currencyConverter.fromResponseObject(currencyListCurrency)));
            return currencies;
        } catch (IOException e) {
            log.error("Failed to load currencies.", e);
            throw new AbstractoRunTimeException(e);
        }
    }

    @Override
    public Currency getCurrencyForString(String input) {
        List<Currency> supportedCurrencies = self.getSupportedCurrencies();
        String lowerInput = input.toLowerCase();
        return supportedCurrencies
            .stream()
            .filter(currency -> currency.getCode().toLowerCase().equals(lowerInput)
                || currency.getName().toLowerCase().equals(lowerInput)
                || currency.getSymbol().toLowerCase().equals(lowerInput)
                || currency.getSymbolNative().toLowerCase().equals(lowerInput))
            .findFirst()
            .orElseThrow(CurrencyNotFoundException::new);
    }

    @Override
    public Double convertCurrency(String sourceCurrencyString, String targetCurrencyString, Double amount) {
        Currency sourceCurrency = getCurrencyForString(sourceCurrencyString);
        Currency targetCurrency = getCurrencyForString(targetCurrencyString);
        return convertCurrency(sourceCurrency, targetCurrency, amount);
    }

    @Override
    @Cacheable(value = "currency-conversion-cache")
    public Map<String, Double> getExchangeRates(Currency sourceCurrency) {
        String formattedUrl = String.format(conversionUrl, sourceCurrency.getCode());
        Request request = new Request.Builder()
            .url(formattedUrl)
            .header("apikey", apiKey)
            .get()
            .build();
        log.info("Loading exchange rate for {}.", sourceCurrency);
        try {
            Response response = okHttpClient.newCall(request).execute();
            CurrencyLatestExchangeResponse currencyLatestExchangeResponse = gson.fromJson(response.body().string(), CurrencyLatestExchangeResponse.class);
            return currencyLatestExchangeResponse.getData();
        } catch (IOException e) {
            log.error("Failed to load currency exchange rate for {}.", sourceCurrency, e);
            throw new AbstractoRunTimeException(e);
        }
    }

    @Override
    public Double convertCurrency(Currency sourceCurrency, Currency targetCurrency, Double amount) {
        Map<String, Double> receivedCurrencies = self.getExchangeRates(sourceCurrency);
        if(!receivedCurrencies.containsKey(targetCurrency.getCode())) {
            throw new CurrencyNotFoundException();
        }
        Double targetCurrencyValue = receivedCurrencies.get(targetCurrency.getCode());
        return amount * targetCurrencyValue;
    }
}
