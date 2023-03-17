package dev.sheldan.abstracto.webservices.openweathermap.service;

import dev.sheldan.abstracto.webservices.openweathermap.model.GeoCodingLocation;
import dev.sheldan.abstracto.webservices.openweathermap.model.GeoCodingResult;
import dev.sheldan.abstracto.webservices.openweathermap.model.WeatherResult;

import java.io.IOException;

public interface OpenWeatherMapService {
    GeoCodingResult searchForLocation(String query) throws IOException;
    WeatherResult retrieveWeatherForLocation(GeoCodingLocation geoCodingLocation, String languageKey) throws IOException;
}
