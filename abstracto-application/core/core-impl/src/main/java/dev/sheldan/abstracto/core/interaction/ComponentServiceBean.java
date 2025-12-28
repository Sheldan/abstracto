package dev.sheldan.abstracto.core.interaction;

import dev.sheldan.abstracto.core.interaction.button.ButtonConfigModel;
import dev.sheldan.abstracto.core.service.MessageService;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.tree.MessageComponentTree;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ComponentServiceBean implements ComponentService {

    @Autowired
    private MessageService messageService;

    @Override
    public String generateComponentId(Long serverId) {
        return generateComponentId();
    }

    @Override
    public String generateComponentId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public CompletableFuture<Message> clearComponents(Message message) {
        MessageComponentTree tree = message.getComponentTree().replace(oldComponent -> null);
        return messageService.editMessage(message, tree);
    }

    @Override
    public CompletableFuture<Message> disableAllComponents(Message message) {
        return setAllComponentStatesTo(message, true);
    }

    @Override
    public CompletableFuture<Message> enableAllComponents(Message message) {
        return setAllComponentStatesTo(message, false);
    }

    @Override
    public CompletableFuture<Message> removeComponentById(Message message, String componentId) {
        MessageComponentTree tree;
        tree = message.getComponentTree().replace(oldComponent -> {
            if (oldComponent instanceof Button && componentId.equals(((Button) oldComponent).getCustomId())) {
                return null;
            } else {
                return oldComponent;
            }
        });
        return messageService.editMessage(message, tree);
    }


    @Override
    public ButtonConfigModel createButtonConfigModel() {
        return ButtonConfigModel.builder().buttonId(generateComponentId()).build();
    }

    private CompletableFuture<Message> setAllComponentStatesTo(Message message, Boolean disabled) {
        MessageComponentTree tree;
        if(disabled) {
            tree = message.getComponentTree().asDisabled();
        } else {
            tree = message.getComponentTree().asEnabled();
        }
        return messageService.editMessage(message, tree);
    }

}
