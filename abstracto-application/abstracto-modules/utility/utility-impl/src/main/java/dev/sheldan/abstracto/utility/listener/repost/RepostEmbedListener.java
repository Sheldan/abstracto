package dev.sheldan.abstracto.utility.listener.repost;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.listener.MessageEmbeddedListener;
import dev.sheldan.abstracto.core.models.listener.GuildMessageEmbedEventModel;
import dev.sheldan.abstracto.utility.config.features.UtilityFeature;
import dev.sheldan.abstracto.utility.service.RepostCheckChannelService;
import dev.sheldan.abstracto.utility.service.RepostService;
import dev.sheldan.abstracto.utility.service.management.PostedImageManagement;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RepostEmbedListener implements MessageEmbeddedListener {

    @Autowired
    private RepostCheckChannelService repostCheckChannelService;

    @Autowired
    private RepostService repostService;

    @Autowired
    private PostedImageManagement repostManagement;

    @Override
    public void execute(GuildMessageEmbedEventModel eventModel) {
        if(repostCheckChannelService.duplicateCheckEnabledForChannel(eventModel.getChannel())) {
            if(repostManagement.messageEmbedsHaveBeenCovered(eventModel.getMessageId())) {
                log.info("The embeds of the message {} in channel {} in server {} have already been covered by repost check -- ignoring.",
                        eventModel.getMessageId(), eventModel.getChannel().getIdLong(), eventModel.getChannel().getGuild().getIdLong());
                return;
            }
            eventModel.getChannel().retrieveMessageById(eventModel.getMessageId()).queue(message -> {
                List<MessageEmbed> imageEmbeds = eventModel.getEmbeds().stream().filter(messageEmbed -> messageEmbed.getType().equals(EmbedType.IMAGE)).collect(Collectors.toList());
                repostService.processMessageEmbedsRepostCheck(imageEmbeds, message);
            });
        }
    }

    @Override
    public FeatureEnum getFeature() {
        return UtilityFeature.REPOST_DETECTION;
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.MEDIUM;
    }

}
