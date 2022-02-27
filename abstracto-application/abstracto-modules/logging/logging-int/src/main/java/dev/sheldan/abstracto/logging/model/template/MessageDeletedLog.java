package dev.sheldan.abstracto.logging.model.template;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.context.SlimUserInitiatedServerContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Used when rendering the log message when a message was deleted. The template is: "message_deleted_embed"
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class MessageDeletedLog extends SlimUserInitiatedServerContext {
    /**
     * A {@link CachedMessage} representing the deleted message
     */
    private CachedMessage cachedMessage;
}
