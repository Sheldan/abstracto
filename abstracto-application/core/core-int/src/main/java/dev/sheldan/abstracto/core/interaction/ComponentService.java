package dev.sheldan.abstracto.core.interaction;

import dev.sheldan.abstracto.core.interaction.button.ButtonConfigModel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;

import java.util.concurrent.CompletableFuture;

public interface ComponentService {

    Integer MAX_BUTTONS_PER_ROW = ActionRow.getMaxAllowed(Component.Type.BUTTON);
    String generateComponentId(Long serverId);
    String generateComponentId();
    CompletableFuture<Message> clearComponents(Message message);
    CompletableFuture<Message> disableAllComponents(Message message);
    CompletableFuture<Message> enableAllComponents(Message message);
    CompletableFuture<Message> removeComponentById(Message message, String componentId);
    ButtonConfigModel createButtonConfigModel();
}
