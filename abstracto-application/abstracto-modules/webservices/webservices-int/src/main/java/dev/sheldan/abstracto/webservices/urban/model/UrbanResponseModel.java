package dev.sheldan.abstracto.webservices.urban.model;

import dev.sheldan.abstracto.core.models.context.SlimUserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class UrbanResponseModel extends SlimUserInitiatedServerContext {
    private UrbanDefinition definition;
}
