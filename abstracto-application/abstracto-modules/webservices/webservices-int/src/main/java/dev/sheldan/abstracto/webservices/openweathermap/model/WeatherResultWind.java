package dev.sheldan.abstracto.webservices.openweathermap.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeatherResultWind {
    @SerializedName("speed")
    private Float speed;
    @SerializedName("deg")
    private Integer degrees;
    @SerializedName("gust")
    private Float gust;
}
