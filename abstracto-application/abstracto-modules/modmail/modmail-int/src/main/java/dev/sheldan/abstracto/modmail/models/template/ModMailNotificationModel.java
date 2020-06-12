package dev.sheldan.abstracto.modmail.models.template;

import dev.sheldan.abstracto.core.models.FullUser;
import dev.sheldan.abstracto.core.models.context.ServerContext;
import dev.sheldan.abstracto.modmail.models.database.ModMailRole;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * The model used to notify staff members about a newly created mod mail thread. This model contains a dynamic amount of roles which are pinged
 * upon creation of a mod mail thread
 */
@Getter
@Setter
@SuperBuilder
public class ModMailNotificationModel extends ServerContext {
    /**
     * The created {@link ModMailThread} which was just created
     */
    private ModMailThread modMailThread;
    /**
     * The {@link FullUser} for which this thread is about
     */
    private FullUser threadUser;
    /**
     * A list of roles which will be notified upon creation of the mod mail thread.
     */
    private List<ModMailRole> roles;
}
