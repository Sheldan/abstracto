package dev.sheldan.abstracto.core.interactive.setup.payload;

import dev.sheldan.abstracto.core.interactive.DelayedActionConfigContainer;
import dev.sheldan.abstracto.core.models.AServerChannelUserId;
import dev.sheldan.abstracto.core.interaction.button.ButtonPayload;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SetupConfirmationPayload implements ButtonPayload {
    private SetupConfirmationAction action;
    private String featureKey;
    private AServerChannelUserId origin;
    private String otherButtonComponentId;
    private List<DelayedActionConfigContainer> actions;

    public enum SetupConfirmationAction {
        CONFIRM, ABORT
    }
}
