package dev.sheldan.abstracto.moderation.model.template.listener;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ReportReactionNotificationModel {
    private CachedMessage reportedMessage;
    private ServerUser reporter;
    private Integer reportCount;
    private String context;
    private Boolean singularMessage;
}
