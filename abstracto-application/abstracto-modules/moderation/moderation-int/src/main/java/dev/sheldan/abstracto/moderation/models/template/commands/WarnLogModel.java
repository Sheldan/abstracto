package dev.sheldan.abstracto.moderation.models.template.commands;

import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Message;


@Getter
@SuperBuilder
@Setter
public class WarnLogModel extends UserInitiatedServerContext {
    private Message message;
    private WarnModel warning;
}
