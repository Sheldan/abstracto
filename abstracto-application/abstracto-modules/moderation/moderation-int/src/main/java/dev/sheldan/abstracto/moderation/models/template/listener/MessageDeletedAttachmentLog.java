package dev.sheldan.abstracto.moderation.models.template.listener;

import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter @SuperBuilder
public class MessageDeletedAttachmentLog extends UserInitiatedServerContext {
    private String imageUrl;
    private Integer counter;
}
