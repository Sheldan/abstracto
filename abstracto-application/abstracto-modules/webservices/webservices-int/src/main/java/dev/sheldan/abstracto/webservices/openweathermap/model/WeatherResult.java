package dev.sheldan.abstracto.webservices.openweathermap.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WeatherResult {
    @SerializedName("coord")
    private WeatherResultCoordinates coordinates;
    @SerializedName("weather")
    private List<WeatherResultWeatherDescription> weathers;
    @SerializedName("visibility")
    private Integer visibility;
    @SerializedName("main")
    private WeatherResultMain mainWeather;
    @SerializedName("rain")
    private WeatherResultRain rainInfo;
    @SerializedName("snow")
    private WeatherResultSnow snowInfo;
    @SerializedName("dt")
    private Long dayTime;
    @SerializedName("clouds")
    private WeatherResultClouds cloudInfo;
    @SerializedName("sys")
    private WeatherResultSystem systemInfo;
    @SerializedName("timezone")
    private Long timezoneShift;

}
