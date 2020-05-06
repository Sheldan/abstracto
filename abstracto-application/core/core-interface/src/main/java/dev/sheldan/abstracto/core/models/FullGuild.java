package dev.sheldan.abstracto.core.models;

import dev.sheldan.abstracto.core.models.database.AServer;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;

@Setter
@Getter
@Builder
public class FullGuild {
    private AServer server;
    private Guild guild;
}
