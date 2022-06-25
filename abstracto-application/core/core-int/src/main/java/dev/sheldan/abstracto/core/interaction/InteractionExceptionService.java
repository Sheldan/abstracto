package dev.sheldan.abstracto.core.interaction;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListener;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerModel;
import dev.sheldan.abstracto.core.interaction.context.message.listener.MessageContextCommandListener;
import dev.sheldan.abstracto.core.interaction.modal.listener.ModalInteractionListener;
import dev.sheldan.abstracto.core.interaction.modal.listener.ModalInteractionListenerModel;
import dev.sheldan.abstracto.core.models.listener.interaction.MessageContextInteractionModel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

public interface InteractionExceptionService {
    void reportExceptionToInteraction(Throwable exception, ButtonClickedListenerModel interActionContext, ButtonClickedListener executedListener);
    void reportExceptionToInteraction(Throwable exception, MessageContextInteractionModel interActionContext, MessageContextCommandListener executedListener);
    void reportExceptionToInteraction(Throwable exception, ModalInteractionListenerModel interActionContext, ModalInteractionListener executedListener);
    void reportExceptionToInteraction(Throwable exception, IReplyCallback callback, InteractionListener executedListener);

    void reportSlashException(Throwable exception, SlashCommandInteractionEvent event, Command command);
}
