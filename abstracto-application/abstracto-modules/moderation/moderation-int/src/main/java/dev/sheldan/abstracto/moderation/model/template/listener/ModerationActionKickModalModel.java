package dev.sheldan.abstracto.moderation.model.template.listener;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ModerationActionKickModalModel {
    private String modalId;
    private String reasonComponentId;
}
