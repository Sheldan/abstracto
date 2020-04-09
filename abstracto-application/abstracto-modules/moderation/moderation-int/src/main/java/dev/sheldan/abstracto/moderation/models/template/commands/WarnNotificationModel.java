package dev.sheldan.abstracto.moderation.models.template.commands;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class WarnNotificationModel {
    private WarnModel warning;
    private String serverName;
}
