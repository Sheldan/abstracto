package dev.sheldan.abstracto.webservices.openweathermap.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeatherResultMain {
    @SerializedName("temp")
    private Float temperature;
    @SerializedName("feels_like")
    private Float feelsLikeTemperature;
    @SerializedName("temp_min")
    private Float minTemperature;
    @SerializedName("temp_max")
    private Float maxTemperature;
    @SerializedName("pressure")
    private Integer pressure;
    @SerializedName("humidity")
    private Integer humidity;
    @SerializedName("sea_level")
    private Integer seaLevelPressure;
    @SerializedName("grnd_level")
    private Integer groundLevelPressure;
}
