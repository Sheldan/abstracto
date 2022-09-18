package dev.sheldan.abstracto.core.interaction;

import dev.sheldan.abstracto.core.interaction.button.ButtonConfigModel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ComponentService {

    Integer MAX_BUTTONS_PER_ROW = Component.Type.BUTTON.getMaxPerRow();
    String generateComponentId(Long serverId);
    String generateComponentId();
    CompletableFuture<Message> addButtonToMessage(Long messageId, GuildMessageChannel textChannel, String buttonId, String description, String emoteMarkdown, ButtonStyle style);
    CompletableFuture<Void> clearButtons(Message message);
    CompletableFuture<Void> disableAllButtons(Message message);
    CompletableFuture<Void> enableAllButtons(Message message);
    CompletableFuture<Void> removeComponentWithId(Message message, String componentId);
    CompletableFuture<Void> removeComponentWithId(Message message, String componentId, Boolean rearrange);
    List<ActionRow> splitIntoActionRowsMax(List<ActionComponent> components);
    ButtonConfigModel createButtonConfigModel();
}
