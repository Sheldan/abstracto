package dev.sheldan.abstracto.moderation.models.template.listener;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Message;

@Getter @Setter @SuperBuilder
public class MessageEditedLog extends UserInitiatedServerContext {
    private Message messageAfter;
    private CachedMessage messageBefore;
}