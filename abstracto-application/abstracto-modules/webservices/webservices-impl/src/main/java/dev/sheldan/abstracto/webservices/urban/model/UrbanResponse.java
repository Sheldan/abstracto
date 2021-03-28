package dev.sheldan.abstracto.webservices.urban.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UrbanResponse {
    private List<UrbanResponseDefinition> list;
}
