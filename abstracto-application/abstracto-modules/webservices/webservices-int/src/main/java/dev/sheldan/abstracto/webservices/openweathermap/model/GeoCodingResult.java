package dev.sheldan.abstracto.webservices.openweathermap.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class GeoCodingResult {
    private List<GeoCodingLocation> results;
}
