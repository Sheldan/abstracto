package dev.sheldan.abstracto.modmail.model.template;

import dev.sheldan.abstracto.core.utils.ChannelUtils;
import dev.sheldan.abstracto.modmail.model.database.ModMailThread;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * This model is used to notify a staff member that there is already a mod mail thread open for the user
 * and provide a link to the channel associated with the user.
 */
@Getter
@Setter
@Builder
public class ModMailThreadExistsModel {
    private ModMailThread existingModMailThread;

    public String getThreadUrl() {
        return ChannelUtils.buildChannelUrl(existingModMailThread.getServer().getId(), existingModMailThread.getChannel().getId());
    }
}
