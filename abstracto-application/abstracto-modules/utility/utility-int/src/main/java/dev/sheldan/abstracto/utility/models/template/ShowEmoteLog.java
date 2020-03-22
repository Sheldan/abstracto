package dev.sheldan.abstracto.utility.models.template;

import dev.sheldan.abstracto.core.models.UserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Emote;

@Getter
@Setter
@SuperBuilder
public class ShowEmoteLog extends UserInitiatedServerContext {
    private Emote emote;
}
