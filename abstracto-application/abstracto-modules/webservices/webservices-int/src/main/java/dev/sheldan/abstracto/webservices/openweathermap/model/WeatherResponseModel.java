package dev.sheldan.abstracto.webservices.openweathermap.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.time.Instant;

@Getter
@Setter
@Builder
public class WeatherResponseModel {
    private Float temperature;
    private Float minTemperature;
    private Float maxTemperature;
    private String mainWeather;
    private String description;
    private Float feelsLikeTemperature;
    private Integer humidity;
    private Integer pressure;
    private Integer seaLevelPressure;
    private Integer groundLevelPressure;
    private Float rain1H;
    private Float rain3H;
    private Float snow1H;
    private Float snow3H;
    private Integer clouds;
    private Instant sunrise;
    private Instant sunset;
    private Integer visibility;
    private Instant dataCalculationTime;
    private String locationName;
    private String countryKey;
    private Color embedColor;
    private Long locationId;
}
