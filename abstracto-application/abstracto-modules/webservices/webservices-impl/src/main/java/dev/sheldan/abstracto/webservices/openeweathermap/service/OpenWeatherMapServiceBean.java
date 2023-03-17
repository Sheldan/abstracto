package dev.sheldan.abstracto.webservices.openeweathermap.service;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import dev.sheldan.abstracto.webservices.openweathermap.model.GeoCodingLocation;
import dev.sheldan.abstracto.webservices.openweathermap.model.GeoCodingResult;
import dev.sheldan.abstracto.webservices.openweathermap.model.WeatherResult;
import dev.sheldan.abstracto.webservices.openweathermap.service.OpenWeatherMapService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Component
public class OpenWeatherMapServiceBean implements OpenWeatherMapService {

    @Autowired
    private OkHttpClient okHttpClient;

    @Value("${abstracto.feature.webservices.openweatherMap.apiKey}")
    private String apiKey;

    @Value("${abstracto.feature.webservices.openweathermap.geocodingURL}")
    private String geoCodingURL;

    @Value("${abstracto.feature.webservices.openweathermap.weatherDataURL}")
    private String weatherDataURL;

    @Autowired
    private Gson gson;

    @Override
    public GeoCodingResult searchForLocation(String query) throws IOException {
        Type geoCodingType = new TypeToken<ArrayList<GeoCodingLocation>>() {}.getType();
        Request request = new Request.Builder()
                .url(String.format(geoCodingURL, query, 5, apiKey))
                .get()
                .build();
        Response response = okHttpClient.newCall(request).execute();
        List<GeoCodingLocation> result = gson.fromJson(response.body().string(), geoCodingType);
        return GeoCodingResult
                .builder()
                .results(result)
                .build();
    }

    @Override
    public WeatherResult retrieveWeatherForLocation(GeoCodingLocation location, String languageKey) throws IOException {
        Request request = new Request.Builder()
                .url(String.format(weatherDataURL, location.getLatitude(), location.getLongitude(), apiKey, languageKey))
                .get()
                .build();
        Response response = okHttpClient.newCall(request).execute();
        return gson.fromJson(response.body().string(), WeatherResult.class);
    }
}
