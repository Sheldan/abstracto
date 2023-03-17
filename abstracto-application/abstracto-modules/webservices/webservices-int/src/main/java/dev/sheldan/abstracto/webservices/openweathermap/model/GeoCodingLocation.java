package dev.sheldan.abstracto.webservices.openweathermap.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeoCodingLocation {
    @SerializedName("name")
    private String name;
    @SerializedName("lat")
    private Double latitude;
    @SerializedName("lon")
    private Double longitude;
    @SerializedName("country")
    private String countryKey;
}
