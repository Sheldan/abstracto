package dev.sheldan.abstracto.core.models.context;

import dev.sheldan.abstracto.core.models.database.AServer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Guild;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class ServerContext {
    private Guild guild;
    private AServer server;

}
