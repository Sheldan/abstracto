package dev.sheldan.abstracto.core.interaction.modal;

import com.google.gson.Gson;
import dev.sheldan.abstracto.core.interaction.modal.config.ModalComponent;
import dev.sheldan.abstracto.core.interaction.modal.config.ModalConfig;
import dev.sheldan.abstracto.core.interaction.modal.config.TextInputComponent;
import dev.sheldan.abstracto.core.interaction.modal.config.TextInputComponentStyle;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import net.dv8tion.jda.api.components.ModalTopLevelComponent;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.modals.Modal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class ModalServiceBean implements ModalService {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private Gson gson;

    @Override
    public CompletableFuture<Void> replyModal(GenericCommandInteractionEvent event, String templateKey, Object model) {
        Modal modal = createModalFromTemplate(templateKey, model, event.getGuild().getIdLong());
        return event.replyModal(modal).submit();
    }

    @Override
    public CompletableFuture<Void> replyModal(ButtonInteractionEvent event, String templateKey, Object model) {
        Modal modal = createModalFromTemplate(templateKey, model, event.getGuild().getIdLong());
        return event.replyModal(modal).submit();
    }

    @Override
    public Modal createModalFromTemplate(String templateKey, Object model, Long serverId) {
        String modalConfigString = templateService.renderTemplate(templateKey + "_modal", model, serverId);
        ModalConfig modalConfig = gson.fromJson(modalConfigString, ModalConfig.class);
        List<ModalComponent> components = modalConfig
                .getTextInputs()
                .stream()
                .sorted(Comparator.comparing(ModalComponent::getPosition))
                .collect(Collectors.toList());
        return Modal.create(modalConfig.getId(), modalConfig.getTitle())
                .addComponents(convertToActionRows(components))
                .build();
    }

    private List<ModalTopLevelComponent> convertToActionRows(List<ModalComponent> components) {
        return components
                .stream()
                .map(this::convertComponent)
                .collect(Collectors.toList());
    }

    private ModalTopLevelComponent convertComponent(ModalComponent component) {
        if(component instanceof TextInputComponent) {
            TextInputComponent tic = (TextInputComponent) component;
            TextInput.Builder tiBuilder =  TextInput.create(tic.getId(), TextInputComponentStyle.getStyle(tic.getStyle()));
            if(tic.getMinLength() != null) {
                tiBuilder.setMinLength(tic.getMinLength());
            }
            if(tic.getMaxLength() != null) {
                tiBuilder.setMaxLength(tic.getMaxLength());
            }
            if(tic.getRequired() != null) {
                tiBuilder.setRequired(tic.getRequired());
            }
            return Label.of(tic.getLabel(), tiBuilder.build());
        }
        return null;
    }


}
