package dev.sheldan.abstracto.modmail.models.template;

import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import dev.sheldan.abstracto.core.utils.ChannelUtils;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class ModMailThreadExistsModel extends UserInitiatedServerContext {
    private ModMailThread existingModMailThread;

    public String getThreadUrl() {
        return ChannelUtils.buildChannelUrl(existingModMailThread.getServer().getId(), existingModMailThread.getChannel().getId());
    }
}
