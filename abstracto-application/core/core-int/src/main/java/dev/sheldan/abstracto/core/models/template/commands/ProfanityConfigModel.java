package dev.sheldan.abstracto.core.models.template.commands;

import dev.sheldan.abstracto.core.models.database.ProfanityGroup;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ProfanityConfigModel {
    private List<ProfanityGroup> profanityGroups;
}
