package dev.sheldan.abstracto.core.interactive;

import dev.sheldan.abstracto.core.models.AServerChannelUserId;

public interface AutoDelayedAction {
    DelayedActionConfig getDelayedActionConfig(AServerChannelUserId aServerChannelUserId);
}
