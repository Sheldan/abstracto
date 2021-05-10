package dev.sheldan.abstracto.repostdetection.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMessageReceivedListener;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.listener.MessageReceivedModel;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.repostdetection.config.RepostDetectionFeatureDefinition;
import dev.sheldan.abstracto.repostdetection.service.RepostCheckChannelService;
import dev.sheldan.abstracto.repostdetection.service.RepostService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
@Slf4j
public class RepostMessageReceivedListener implements AsyncMessageReceivedListener {

    @Autowired
    private RepostCheckChannelService repostCheckChannelService;

    @Autowired
    private RepostService repostService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Override
    public DefaultListenerResult execute(MessageReceivedModel model) {
        Message message = model.getMessage();
        if(!message.isFromGuild() || message.isWebhookMessage() || message.getType().isSystem()) {
            return DefaultListenerResult.IGNORED;
        }
        AChannel channel = channelManagementService.loadChannel(message.getTextChannel().getIdLong());
        if(repostCheckChannelService.duplicateCheckEnabledForChannel(channel)) {
            repostService.processMessageAttachmentRepostCheck(message);
            List<MessageEmbed> imageEmbeds = message.getEmbeds().stream().filter(messageEmbed -> messageEmbed.getType().equals(EmbedType.IMAGE)).collect(Collectors.toList());
            repostService.processMessageEmbedsRepostCheck(imageEmbeds, message);
            return DefaultListenerResult.PROCESSED;
        }
        return DefaultListenerResult.IGNORED;
    }

    @Override
    public FeatureDefinition getFeature() {
        return RepostDetectionFeatureDefinition.REPOST_DETECTION;
    }

}
