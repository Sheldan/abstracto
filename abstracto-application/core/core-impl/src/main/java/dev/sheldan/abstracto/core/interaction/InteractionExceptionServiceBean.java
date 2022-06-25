package dev.sheldan.abstracto.core.interaction;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListener;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerModel;
import dev.sheldan.abstracto.core.interaction.context.message.listener.MessageContextCommandListener;
import dev.sheldan.abstracto.core.interaction.modal.listener.ModalInteractionListener;
import dev.sheldan.abstracto.core.interaction.modal.listener.ModalInteractionListenerModel;
import dev.sheldan.abstracto.core.models.listener.interaction.MessageContextInteractionModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InteractionExceptionServiceBean implements InteractionExceptionService {

    @Autowired
    private InteractionService interactionService;

    public static final String GENERIC_INTERACTION_EXCEPTION = "generic_interaction_exception";

    @Override
    public void reportExceptionToInteraction(Throwable exception, ButtonClickedListenerModel interactionContext, ButtonClickedListener executedListener) {
        reportExceptionToInteraction(exception, interactionContext.getEvent(), executedListener);
    }

    @Override
    public void reportExceptionToInteraction(Throwable exception, MessageContextInteractionModel interActionContext, MessageContextCommandListener executedListener) {
        reportExceptionToInteraction(exception, interActionContext.getEvent(), executedListener);
    }

    @Override
    public void reportExceptionToInteraction(Throwable exception, ModalInteractionListenerModel interActionContext, ModalInteractionListener executedListener) {
        reportExceptionToInteraction(exception, interActionContext.getEvent().getInteraction(), executedListener);
    }

    @Override
    public void reportExceptionToInteraction(Throwable exception, IReplyCallback callback, InteractionListener executedListener) {
        if(executedListener != null) {
            log.info("Reporting generic exception {} of listener {} towards channel {} in server {}.",
                    exception.getClass().getSimpleName(), executedListener.getClass().getSimpleName(), callback.getChannel().getIdLong(),
                    callback.getGuild().getIdLong());
        } else {
            log.info("Reporting generic exception {} towards channel {} in server {}.",
                    exception.getClass().getSimpleName(), callback.getChannel().getIdLong(),
                    callback.getGuild().getIdLong());
        }
        try {
            reportGenericInteractionException(exception, callback);
        } catch (Exception e) {
            log.error("Failed to notify about exception.", e);
        }
    }

    @Override
    public void reportSlashException(Throwable exception, SlashCommandInteractionEvent event, Command command) {
        log.info("Reporting exception of {} command {} in channel {} in guild {} from user {}.",
                exception.getClass().getSimpleName(), command.getConfiguration().getName(),
                event.getChannel().getIdLong(), event.getGuild().getIdLong(), event.getMember().getIdLong(), exception);
        reportGenericInteractionException(exception, event.getInteraction());
    }

    private void reportGenericInteractionException(Throwable throwable, IReplyCallback replyCallback) {
        GenericInteractionExceptionModel exceptionModel = buildInteractionExceptionModel(throwable, replyCallback);
        if(replyCallback.isAcknowledged()) {
            interactionService.sendMessageToInteraction(GENERIC_INTERACTION_EXCEPTION, exceptionModel, replyCallback.getHook());
        } else {
            interactionService.replyEmbed(GENERIC_INTERACTION_EXCEPTION, exceptionModel, replyCallback);
        }
    }

    private GenericInteractionExceptionModel buildInteractionExceptionModel(Throwable throwable, IReplyCallback context) {
        return GenericInteractionExceptionModel
                .builder()
                .member(context.getMember())
                .user(context.getUser())
                .throwable(throwable)
                .build();
    }
}
