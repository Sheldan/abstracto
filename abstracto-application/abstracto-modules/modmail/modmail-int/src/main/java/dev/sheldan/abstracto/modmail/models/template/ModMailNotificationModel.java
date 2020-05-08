package dev.sheldan.abstracto.modmail.models.template;

import dev.sheldan.abstracto.core.models.FullUser;
import dev.sheldan.abstracto.core.models.context.ServerContext;
import dev.sheldan.abstracto.modmail.models.database.ModMailRole;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
public class ModMailNotificationModel extends ServerContext {
    private ModMailThread modMailThread;
    private FullUser threadUser;
    private List<ModMailRole> roles;
}
