package dev.sheldan.abstracto.moderation.models.template.listener;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter @SuperBuilder
public class MessageDeletedLog extends UserInitiatedServerContext {
    private CachedMessage message;
}
