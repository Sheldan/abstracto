package dev.sheldan.abstracto.moderation.model.template.listener;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.context.SlimUserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Used when rendering the log message when a message was deleted. The template is: "message_deleted_embed"
 */
@Getter
@Setter
@SuperBuilder
public class MessageDeletedLog extends SlimUserInitiatedServerContext {
    /**
     * A {@link CachedMessage} representing the deleted message
     */
    private CachedMessage cachedMessage;
}
