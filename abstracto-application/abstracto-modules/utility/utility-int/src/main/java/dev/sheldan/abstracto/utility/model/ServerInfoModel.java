package dev.sheldan.abstracto.utility.model;

import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import dev.sheldan.abstracto.core.models.template.display.EmoteDisplay;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Guild;

import java.util.List;

@Getter
@Setter
@SuperBuilder
public class ServerInfoModel extends UserInitiatedServerContext {
    private Guild guild;
    private List<EmoteDisplay> staticEmotes;
    private List<EmoteDisplay> animatedEmotes;
}
