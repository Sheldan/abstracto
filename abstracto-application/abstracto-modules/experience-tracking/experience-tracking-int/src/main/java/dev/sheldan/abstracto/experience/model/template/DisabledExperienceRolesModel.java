package dev.sheldan.abstracto.experience.model.template;

import dev.sheldan.abstracto.core.models.FullRole;
import lombok.Builder;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.List;

/**
 * Model used to render  an overview of the roles for which experience gain has been disabled on the current server.
 */
@Getter
@Builder
public class DisabledExperienceRolesModel {
    /**
     * A list of {@link FullRole roles} for which experience gain is disabled in this server
     */
    @Builder.Default
    private List<FullRole> roles = new ArrayList<>();
    private Member member;
}
