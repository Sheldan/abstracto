package dev.sheldan.abstracto.core.service;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class ComponentServiceBean implements ComponentService {


    @Autowired
    private MessageService messageService;

    @Autowired
    private ChannelService channelService;

    @Override
    public String generateComponentId(Long serverId) {
        return generateComponentId();
    }

    @Override
    public String generateComponentId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public CompletableFuture<Message> addButtonToMessage(Long messageId, TextChannel textChannel, String buttonId, String description, String emoteMarkdown, ButtonStyle style) {
        return channelService.retrieveMessageInChannel(textChannel, messageId).thenCompose(message -> {
            Button button = Button.of(style, buttonId, description);
            if(emoteMarkdown != null) {
                button = button.withEmoji(Emoji.fromMarkdown(emoteMarkdown));
            }
            List<ActionRow> actionRows;
            if(message.getActionRows().isEmpty()) {
                actionRows = Arrays.asList(ActionRow.of(button));
            } else {
                ActionRow lastRow = message.getActionRows().get(message.getActionRows().size() - 1);
                if(lastRow.getComponents().size() < MAX_BUTTONS_PER_ROW) {
                    lastRow.getComponents().add(button);
                    actionRows = message.getActionRows();
                } else {
                    List<ActionRow> currentActionRows = new ArrayList<>(message.getActionRows());
                    currentActionRows.add(ActionRow.of(button));
                    actionRows = currentActionRows;
                }
            }
            return messageService.editMessageWithActionRowsMessage(message, actionRows);
        });
    }

    @Override
    public CompletableFuture<Void> clearButtons(Message message) {
        return messageService.editMessageWithActionRows(message, new ArrayList<>());
    }

    @Override
    public CompletableFuture<Void> disableAllButtons(Message message) {
        return setAllButtonStatesTo(message, true);
    }

    @Override
    public CompletableFuture<Void> enableAllButtons(Message message) {
        return setAllButtonStatesTo(message, false);
    }

    @Override
    public CompletableFuture<Void> removeComponentWithId(Message message, String componentId) {
       return removeComponentWithId(message, componentId, false);
    }

    @Override
    public CompletableFuture<Void> removeComponentWithId(Message message, String componentId, Boolean rearrange) {
        List<ActionRow> actionRows = new ArrayList<>();
        if(Boolean.TRUE.equals(rearrange)) {
            List<net.dv8tion.jda.api.interactions.components.Component> components = new ArrayList<>();
            message.getActionRows().forEach(row ->
                            row
                                .getComponents()
                                .stream()
                                .filter(component -> component.getId() == null || !component.getId().equals(componentId))
                                .forEach(components::add));
            actionRows = splitIntoActionRowsMax(components);
        } else {
            for (ActionRow row : message.getActionRows()) {
                actionRows.add(ActionRow.of(
                        row
                                .getComponents()
                                .stream()
                                .filter(component -> component.getId() == null || !component.getId().equals(componentId))
                                .collect(Collectors.toList())));
            }
        }
        return messageService.editMessageWithActionRows(message, actionRows);
    }

    @Override
    public List<ActionRow> splitIntoActionRowsMax(List<net.dv8tion.jda.api.interactions.components.Component> allComponents) {
        List<List<net.dv8tion.jda.api.interactions.components.Component>> actionRows = ListUtils.partition(allComponents, MAX_BUTTONS_PER_ROW);
        return actionRows.stream().map(ActionRow::of).collect(Collectors.toList());
    }

    private CompletableFuture<Void> setAllButtonStatesTo(Message message, Boolean disabled) {
        List<ActionRow> actionRows = new ArrayList<>();

        message.getActionRows().forEach(row -> {
            List<net.dv8tion.jda.api.interactions.components.Component> newComponents = new ArrayList<>();
            row.getComponents().forEach(component -> {
                if(component.getType().equals(net.dv8tion.jda.api.interactions.components.Component.Type.BUTTON)) {
                    Button button = ((Button) component).withDisabled(disabled);
                    newComponents.add(button);
                } else {
                    newComponents.add(component);
                }
            });
            actionRows.add(ActionRow.of(newComponents));
        });
        return messageService.editMessageWithActionRows(message, actionRows);
    }

}
