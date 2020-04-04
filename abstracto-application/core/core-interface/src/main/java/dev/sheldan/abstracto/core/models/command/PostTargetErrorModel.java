package dev.sheldan.abstracto.core.models.command;

import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Setter
@Getter
@SuperBuilder
public class PostTargetErrorModel extends UserInitiatedServerContext {
    private List<String> validPostTargets;
}
