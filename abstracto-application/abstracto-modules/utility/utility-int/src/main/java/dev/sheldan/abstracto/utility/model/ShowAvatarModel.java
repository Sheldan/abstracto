package dev.sheldan.abstracto.utility.model;

import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Member;

@Getter
@Setter
@SuperBuilder
public class ShowAvatarModel extends UserInitiatedServerContext {
    private Member memberInfo;
}
