package dev.sheldan.abstracto.moderation.models.template.job;

import dev.sheldan.abstracto.core.models.context.ServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
public class WarnDecayLogModel extends ServerContext {
    private List<WarnDecayWarning> warnings;
}
