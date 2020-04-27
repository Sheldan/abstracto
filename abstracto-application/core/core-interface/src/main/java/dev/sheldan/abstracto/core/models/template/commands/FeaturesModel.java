package dev.sheldan.abstracto.core.models.template.commands;

import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
@Setter
public class FeaturesModel extends UserInitiatedServerContext {
    private List<FeatureFlagDisplay> features;
}
