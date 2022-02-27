package dev.sheldan.abstracto.core.interactive;

import dev.sheldan.abstracto.core.interactive.setup.callback.MessageInteractionCallback;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.listener.MessageReceivedModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Slf4j
public class InteractiveServiceBean implements InteractiveService {

    @Autowired
    private ChannelService channelService;

    @Autowired
    private InteractiveMessageReceivedListener interactiveMessageReceivedListener;

    @Override
    public void createMessageWithResponse(String messageText, AUserInAServer responder, AChannel channel, Consumer<MessageReceivedModel> action, Consumer<MessageReceivedModel> timeoutAction) {
        channelService.sendTextToAChannel(messageText, channel);
        createMessageReceivedCallback(channel, responder, action, timeoutAction);
    }

    @Override
    public void createMessageWithResponse(MessageToSend messageToSend, AUserInAServer responder, AChannel channel,
                                          Consumer<MessageReceivedModel> action, Consumer<MessageReceivedModel> timeoutAction) {
        channelService.sendMessageEmbedToSendToAChannel(messageToSend, channel);
        createMessageReceivedCallback(channel, responder, action, timeoutAction);
    }

    private void createMessageReceivedCallback(AChannel channel, AUserInAServer responder, Consumer<MessageReceivedModel> action,  Consumer<MessageReceivedModel> timeoutAction) {
        MessageInteractionCallback callBack = MessageInteractionCallback
                .builder()
                .serverId(channel.getServer().getId())
                .channelId(channel.getId())
                .userId(responder.getUserReference().getId())
                .action(action)
                .timeoutAction(timeoutAction)
                .build();
        interactiveMessageReceivedListener.addCallback(callBack);
    }

}
