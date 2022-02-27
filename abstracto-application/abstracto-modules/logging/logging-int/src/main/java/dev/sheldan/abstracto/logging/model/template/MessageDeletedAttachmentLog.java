package dev.sheldan.abstracto.logging.model.template;

import dev.sheldan.abstracto.core.models.context.SlimUserInitiatedServerContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Used when rendering the attachment message, when the message contained multiple attachments.
 * The template is: "message_deleted_attachment_embed"
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class MessageDeletedAttachmentLog extends SlimUserInitiatedServerContext {
    /**
     * The proxy URL to the attachment which was deleted.
     */
    private String imageUrl;
    /**
     * The index of this attachment in the deleted message.
     */
    private Integer counter;
}
