package dev.sheldan.abstracto.moderation.model.template.listener;

import dev.sheldan.abstracto.core.interaction.button.ButtonPayload;
import dev.sheldan.abstracto.core.models.ServerUser;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ModerationActionPayloadModel implements ButtonPayload {
    private String action;
    private ServerUser user;

    public static ModerationActionPayloadModel forAction(String action, ServerUser user) {
        return ModerationActionPayloadModel
                .builder()
                .user(user)
                .action(action)
                .build();
    }
}
