package dev.sheldan.abstracto.core.command.listener;

import dev.sheldan.abstracto.core.command.CommandReceivedHandler;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.DriedCommandContext;
import dev.sheldan.abstracto.core.command.model.CommandConfirmationPayload;
import dev.sheldan.abstracto.core.command.model.CommandServiceBean;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.listener.ButtonClickedListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.ButtonClickedListener;
import dev.sheldan.abstracto.core.models.listener.ButtonClickedListenerModel;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.service.management.ComponentPayloadManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class ConfirmationButtonClickedListener implements ButtonClickedListener {

    @Autowired
    private CommandServiceBean commandServiceBean;

    @Autowired
    private CommandReceivedHandler commandReceivedHandler;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ComponentPayloadManagementService componentPayloadManagementService;

    @Autowired
    private ConfirmationButtonClickedListener self;

    @Autowired
    private InteractionService interactionService;

    @Override
    public ButtonClickedListenerResult execute(ButtonClickedListenerModel model) {
        CommandConfirmationPayload payload = (CommandConfirmationPayload) model.getDeserializedPayload();
        DriedCommandContext commandCtx = payload.getCommandContext();
        if(payload.getAction().equals(CommandConfirmationPayload.CommandConfirmationAction.CONFIRM)) {
            log.info("Confirming command {} in server {} from message {} in channel {} with event {}.",
                    commandCtx.getCommandName(), commandCtx.getServerId(), commandCtx.getMessageId(),
                    commandCtx.getChannelId(), model.getEvent().getInteraction().getId());
            commandServiceBean.fillCommandContext(commandCtx)
                    .thenAccept(context -> self.executeButtonClickedListener(model, payload, context))
                    .exceptionally(throwable -> {
                        log.error("Command confirmation failed to execute.", throwable);
                        return null;
                    });
        } else {
            log.info("Denying command {} in server {} from message {} in channel {} with event {}.",
                    commandCtx.getCommandName(), commandCtx.getServerId(), commandCtx.getMessageId(),
                    commandCtx.getChannelId(), model.getEvent().getInteraction().getId());
        cleanup(model, payload)
            .thenAccept(unused -> self.sendAbortNotification(model))
            .exceptionally(throwable -> {
                log.warn("Failed to clean up confirmation message {}.", model.getEvent().getMessageId());
                return null;
            });
        }
        return ButtonClickedListenerResult.ACKNOWLEDGED;
    }

    @Transactional
    public void executeButtonClickedListener(ButtonClickedListenerModel model, CommandConfirmationPayload payload, CommandServiceBean.RebuiltCommandContext context) {
        try {
            if(context.getCommand().getConfiguration().isAsync()) {
                commandReceivedHandler.executeAsyncCommand(context.getCommand(), context.getContext());
            } else {
                CommandResult result = commandReceivedHandler.executeCommand(context.getCommand(), context.getContext());
                commandReceivedHandler.executePostCommandListener(context.getCommand(), context.getContext(), result);
            }
        } catch (Exception e) {
            commandReceivedHandler.reportException(context.getContext(), context.getCommand(), e, "Confirmation execution of command failed.");
        } finally {
            cleanup(model, payload);
        }
    }

    private CompletableFuture<Void> cleanup(ButtonClickedListenerModel model, CommandConfirmationPayload payload) {
        log.debug("Cleaning up component {} and {}.", payload.getOtherButtonComponentId(), model.getEvent().getComponentId());
        componentPayloadManagementService.deletePayloads(Arrays.asList(payload.getOtherButtonComponentId(), model.getEvent().getComponentId()));
        log.debug("Deleting confirmation message {}.", model.getEvent().getMessageId());
        return messageService.deleteMessage(model.getEvent().getMessage());
    }

    public CompletableFuture<Void> sendAbortNotification(ButtonClickedListenerModel model) {
        log.info("Sending abort notification for message {}", model.getEvent().getMessageId());
        return FutureUtils.toSingleFutureGeneric(interactionService.sendMessageToInteraction("command_aborted_notification", new Object(), model.getEvent().getHook()));
    }

    @Override
    public Boolean handlesEvent(ButtonClickedListenerModel model) {
        return model.getOrigin().equals(CommandReceivedHandler.COMMAND_CONFIRMATION_ORIGIN);
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.MEDIUM;
    }
}
