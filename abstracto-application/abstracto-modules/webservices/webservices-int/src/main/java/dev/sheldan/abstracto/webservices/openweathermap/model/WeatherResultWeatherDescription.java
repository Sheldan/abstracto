package dev.sheldan.abstracto.webservices.openweathermap.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeatherResultWeatherDescription {
    private Long id;
    private String main;
    private String description;
    private String icon;
}
