package dev.sheldan.abstracto.moderation.models.template.listener;

import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Used when rendering the attachment message, when the message contained multiple attachments.
 * The template is: "message_deleted_attachment_embed"
 */
@Getter
@Setter
@SuperBuilder
public class MessageDeletedAttachmentLog extends UserInitiatedServerContext {
    /**
     * The proxy URL to the attachment which was deleted.
     */
    private String imageUrl;
    /**
     * The index of this attachment in the deleted message.
     */
    private Integer counter;
}
