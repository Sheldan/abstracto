package dev.sheldan.abstracto.moderation.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ModerationActionButton {
    private String componentId;
    private String action;
}
