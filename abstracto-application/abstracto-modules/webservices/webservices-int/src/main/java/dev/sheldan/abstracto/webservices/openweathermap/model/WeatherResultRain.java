package dev.sheldan.abstracto.webservices.openweathermap.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeatherResultRain {
    @SerializedName("1h")
    private Float rain1H;

    @SerializedName("3h")
    private Float rain3H;
}
