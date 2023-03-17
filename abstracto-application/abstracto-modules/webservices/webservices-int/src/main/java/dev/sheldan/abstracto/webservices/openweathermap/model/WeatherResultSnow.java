package dev.sheldan.abstracto.webservices.openweathermap.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeatherResultSnow {
    @SerializedName("1h")
    private Float snow1H;

    @SerializedName("3h")
    private Float snow3H;
}
