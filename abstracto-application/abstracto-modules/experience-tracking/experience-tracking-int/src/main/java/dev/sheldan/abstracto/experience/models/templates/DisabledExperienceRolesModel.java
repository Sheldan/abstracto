package dev.sheldan.abstracto.experience.models.templates;

import dev.sheldan.abstracto.core.models.FullRole;
import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
public class DisabledExperienceRolesModel extends UserInitiatedServerContext {
    @Builder.Default
    private List<FullRole> roles = new ArrayList<>();
}
