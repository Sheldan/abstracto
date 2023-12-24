package dev.sheldan.abstracto.modmail.model.template;

import dev.sheldan.abstracto.core.models.FullUserInServer;
import dev.sheldan.abstracto.core.models.context.ServerContext;
import dev.sheldan.abstracto.modmail.model.database.ModMailRole;
import dev.sheldan.abstracto.modmail.model.database.ModMailThread;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

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
     * The {@link FullUserInServer} for which this thread is about
     */
    private Member member;
    /**
     * A list of roles which will be notified upon creation of the mod mail thread.
     */
    private List<ModMailRole> roles;
    /**
     * The {@link GuildMessageChannel} in which the mod mail thread is handled
     */
    private GuildMessageChannel channel;
}
