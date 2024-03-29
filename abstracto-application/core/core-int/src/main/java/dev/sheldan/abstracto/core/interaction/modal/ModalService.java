package dev.sheldan.abstracto.core.interaction.modal;

import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.util.concurrent.CompletableFuture;

public interface ModalService {
    CompletableFuture<Void> replyModal(GenericCommandInteractionEvent event, String templateKey, Object model);
    CompletableFuture<Void> replyModal(ButtonInteractionEvent event, String templateKey, Object model);
    Modal createModalFromTemplate(String templateKey, Object model, Long serverId);
}
