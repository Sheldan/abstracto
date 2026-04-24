package dev.sheldan.abstracto.modmail.listener;

import dev.sheldan.abstracto.core.Prioritized;
import dev.sheldan.abstracto.core.listener.AbstractoListener;
import dev.sheldan.abstracto.core.listener.ListenerExecutionResult;
import dev.sheldan.abstracto.modmail.model.listener.ModmailThreadActionListenerModel;

public interface ModmailThreadActionListener extends
    AbstractoListener<ModmailThreadActionListenerModel, ModmailThreadActionListener.ModmailThreadActionListenerResult>, Prioritized {

    enum ModmailThreadActionListenerResult implements ListenerExecutionResult {
        FINAL, IGNORED, PROCESSED
    }
}
