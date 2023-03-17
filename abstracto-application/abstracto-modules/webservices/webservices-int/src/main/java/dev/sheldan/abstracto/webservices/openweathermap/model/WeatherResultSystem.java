package dev.sheldan.abstracto.webservices.openweathermap.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeatherResultSystem {
    @SerializedName("country")
    private String country;

    @SerializedName("sunrise")
    private Long sunrise;

    @SerializedName("sunset")
    private Long sunset;
}
