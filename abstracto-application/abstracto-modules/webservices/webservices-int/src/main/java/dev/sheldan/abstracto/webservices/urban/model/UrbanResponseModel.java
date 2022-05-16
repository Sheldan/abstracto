package dev.sheldan.abstracto.webservices.urban.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UrbanResponseModel {
    private UrbanDefinition definition;
}
