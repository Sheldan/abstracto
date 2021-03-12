package dev.sheldan.abstracto.repostdetection.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMessageEmbeddedListener;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.listener.GuildMessageEmbedEventModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.repostdetection.config.RepostDetectionFeatureDefinition;
import dev.sheldan.abstracto.repostdetection.service.RepostCheckChannelService;
import dev.sheldan.abstracto.repostdetection.service.RepostService;
import dev.sheldan.abstracto.repostdetection.service.management.PostedImageManagement;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RepostEmbedListener implements AsyncMessageEmbeddedListener {

    @Autowired
    private RepostCheckChannelService repostCheckChannelService;

    @Autowired
    private RepostService repostService;

    @Autowired
    private PostedImageManagement repostManagement;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private ChannelService channelService;

    @Override
    public void execute(GuildMessageEmbedEventModel eventModel) {
        AChannel channel = channelManagementService.loadChannel(eventModel.getChannelId());
        if(repostCheckChannelService.duplicateCheckEnabledForChannel(channel)) {
            if(repostManagement.messageEmbedsHaveBeenCovered(eventModel.getMessageId())) {
                log.info("The embeds of the message {} in channel {} in server {} have already been covered by repost check -- ignoring.",
                        eventModel.getMessageId(), eventModel.getChannelId(), eventModel.getServerId());
                return;
            }
            channelService.retrieveMessageInChannel(eventModel.getServerId(), eventModel.getChannelId(), eventModel.getMessageId()).thenAccept(message -> {
                List<MessageEmbed> imageEmbeds = eventModel.getEmbeds().stream().filter(messageEmbed -> messageEmbed.getType().equals(EmbedType.IMAGE)).collect(Collectors.toList());
                if(!imageEmbeds.isEmpty()) {
                    repostService.processMessageEmbedsRepostCheck(imageEmbeds, message);
                }
            });
        }
    }

    @Override
    public FeatureDefinition getFeature() {
        return RepostDetectionFeatureDefinition.REPOST_DETECTION;
    }

}
