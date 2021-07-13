package dev.sheldan.abstracto.antiraid.model;

import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MassPingNotificationModel {
    private MemberDisplay memberDisplay;
    private String messageContent;
    private String messageLink;
    private Integer mentionCount;
}
