package dev.sheldan.abstracto.utility.model;

import dev.sheldan.abstracto.core.models.template.display.EmoteDisplay;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
public class ServerInfoModel {
    private Guild guild;
    private Instant timeCreated;
    private List<EmoteDisplay> staticEmotes;
    private List<EmoteDisplay> animatedEmotes;
}
