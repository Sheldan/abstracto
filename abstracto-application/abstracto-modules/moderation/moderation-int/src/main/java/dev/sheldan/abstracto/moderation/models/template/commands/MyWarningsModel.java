package dev.sheldan.abstracto.moderation.models.template.commands;

import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class MyWarningsModel extends UserInitiatedServerContext {
    private Boolean serverUsesDecays;
    private Long totalWarnCount;
    private Long currentWarnCount;
}
