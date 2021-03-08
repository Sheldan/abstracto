package dev.sheldan.abstracto.core.models.template.commands;

import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
public class PostTargetDisplayModel extends UserInitiatedServerContext {
    private List<PostTargetModelEntry> postTargets;
}
