package dev.sheldan.abstracto.core.interaction.slash.listener;

import static dev.sheldan.abstracto.core.interaction.slash.SlashCommandListenerBean.SLASH_COMMAND_CONFIRMATION_ORIGIN;

import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListener;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerModel;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerResult;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandListenerBean;
import dev.sheldan.abstracto.core.interaction.slash.payload.SlashCommandConfirmationPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SlashCommandConfirmationGivenButtonListener implements ButtonClickedListener {

    @Autowired
    private SlashCommandListenerBean slashCommandListenerBean;


    @Override
    public ButtonClickedListenerResult execute(ButtonClickedListenerModel model) {
        SlashCommandConfirmationPayload payload = (SlashCommandConfirmationPayload) model.getDeserializedPayload();
        if(payload.getAction().equals(SlashCommandConfirmationPayload.CommandConfirmationAction.CONFIRM)) {
            slashCommandListenerBean.continueSlashCommand(payload.getInteractionId(), model.getEvent());
            return ButtonClickedListenerResult.ACKNOWLEDGED;
        } else {
            return ButtonClickedListenerResult.IGNORED;
        }
    }

    @Override
    public Boolean handlesEvent(ButtonClickedListenerModel model) {
        return model.getOrigin().equals(SLASH_COMMAND_CONFIRMATION_ORIGIN);
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.HIGHEST;
    }
}
