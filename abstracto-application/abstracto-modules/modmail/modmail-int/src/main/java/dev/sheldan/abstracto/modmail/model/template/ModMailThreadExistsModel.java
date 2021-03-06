package dev.sheldan.abstracto.modmail.model.template;

import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import dev.sheldan.abstracto.core.utils.ChannelUtils;
import dev.sheldan.abstracto.modmail.model.database.ModMailThread;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * This model is used to notify a staff member that there is already a mod mail thread open for the user
 * and provide a link to the channel associated with the user.
 */
@Getter
@Setter
@SuperBuilder
public class ModMailThreadExistsModel extends UserInitiatedServerContext {
    private ModMailThread existingModMailThread;

    public String getThreadUrl() {
        return ChannelUtils.buildChannelUrl(existingModMailThread.getServer().getId(), existingModMailThread.getChannel().getId());
    }
}
