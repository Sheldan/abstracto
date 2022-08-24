package dev.sheldan.abstracto.entertainment.listener.interaction;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.MessageContextConfig;
import dev.sheldan.abstracto.core.interaction.context.ContextCommandService;
import dev.sheldan.abstracto.core.interaction.context.message.listener.MessageContextCommandListener;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.models.listener.interaction.MessageContextInteractionModel;
import dev.sheldan.abstracto.entertainment.config.EntertainmentFeatureDefinition;
import dev.sheldan.abstracto.entertainment.model.command.MockResponseModel;
import dev.sheldan.abstracto.entertainment.service.EntertainmentService;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static dev.sheldan.abstracto.entertainment.command.Mock.MOCK_RESPONSE_TEMPLATE_KEY;


@Component
public class MockMessageContextCommandListener implements MessageContextCommandListener {

    @Autowired
    private ContextCommandService contextCommandService;

    @Autowired
    private EntertainmentService entertainmentService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public DefaultListenerResult execute(MessageContextInteractionModel eventModel) {
        Message targetMessage = eventModel.getEvent().getTarget();
        String mockText = entertainmentService.createMockText(targetMessage.getContentRaw(), eventModel.getEvent().getMember(), targetMessage.getMember());
        MockResponseModel model = MockResponseModel
                .builder()
                .originalText(targetMessage.getContentRaw())
                .mockingText(mockText)
                .build();
        interactionService.replyEmbed(MOCK_RESPONSE_TEMPLATE_KEY, model, eventModel.getEvent());
        return DefaultListenerResult.PROCESSED;
    }

    @Override
    public FeatureDefinition getFeature() {
        return EntertainmentFeatureDefinition.ENTERTAINMENT;
    }

    @Override
    public MessageContextConfig getConfig() {
        return MessageContextConfig
                .builder()
                .isTemplated(true)
                .name("mock")
                .templateKey("mock_message_context_menu_label")
                .build();
    }

    @Override
    public Boolean handlesEvent(MessageContextInteractionModel model) {
        return contextCommandService.matchesGuildContextName(model, getConfig(), model.getServerId());
    }
}
