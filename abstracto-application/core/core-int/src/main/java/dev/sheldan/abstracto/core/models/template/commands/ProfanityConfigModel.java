package dev.sheldan.abstracto.core.models.template.commands;

import dev.sheldan.abstracto.core.models.context.SlimUserInitiatedServerContext;
import dev.sheldan.abstracto.core.models.database.ProfanityGroup;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
public class ProfanityConfigModel extends SlimUserInitiatedServerContext {
    private List<ProfanityGroup> profanityGroups;
}
