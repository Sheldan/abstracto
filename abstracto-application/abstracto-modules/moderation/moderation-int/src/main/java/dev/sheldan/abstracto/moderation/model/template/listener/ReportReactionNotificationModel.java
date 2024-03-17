package dev.sheldan.abstracto.moderation.model.template.listener;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.moderation.model.ModerationActionButton;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ReportReactionNotificationModel {
    private CachedMessage reportedMessage;
    private ServerUser reporter;
    private Integer reportCount;
    private String context;
    private Boolean singularMessage;
    private List<ModerationActionButton> moderationActionComponents;
}
