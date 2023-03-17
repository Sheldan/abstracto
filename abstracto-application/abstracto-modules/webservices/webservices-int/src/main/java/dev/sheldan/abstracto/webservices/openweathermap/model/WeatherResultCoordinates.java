package dev.sheldan.abstracto.webservices.openweathermap.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeatherResultCoordinates {
    @SerializedName("lon")
    private Float longitude;
    @SerializedName("lat")
    private Float latitude;

}
