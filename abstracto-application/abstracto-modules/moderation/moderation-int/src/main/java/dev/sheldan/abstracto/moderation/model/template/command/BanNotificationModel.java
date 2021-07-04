package dev.sheldan.abstracto.moderation.model.template.command;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BanNotificationModel {
    private String serverName;
    private String reason;
}
