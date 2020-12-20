package dev.sheldan.abstracto.moderation.models.template.listener;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Used when rendering the log message when a message was edited. The template is: "message_edited_embed"
 */
@Getter
@Setter
@SuperBuilder
public class MessageEditedLog extends UserInitiatedServerContext {
    /**
     * The {@link CachedMessage} instance which contains the new content of the message
     */
    private CachedMessage messageAfter;

    /**
     * The {@link CachedMessage} which contains the message before the edit was made
     */
    private CachedMessage messageBefore;
}
