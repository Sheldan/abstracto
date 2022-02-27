package dev.sheldan.abstracto.core.models.template.commands;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SetupCompletedNotificationModel {
    private String featureKey;
}
