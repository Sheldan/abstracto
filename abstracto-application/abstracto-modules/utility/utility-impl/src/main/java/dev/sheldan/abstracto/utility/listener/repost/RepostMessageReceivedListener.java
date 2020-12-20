package dev.sheldan.abstracto.utility.listener.repost;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMessageReceivedListener;
import dev.sheldan.abstracto.core.models.cache.CachedEmbed;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.utility.config.features.UtilityFeature;
import dev.sheldan.abstracto.utility.service.RepostCheckChannelService;
import dev.sheldan.abstracto.utility.service.RepostService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.EmbedType;
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
    public void execute(CachedMessage message) {
        AChannel channel = channelManagementService.loadChannel(message.getChannelId());
        if(repostCheckChannelService.duplicateCheckEnabledForChannel(channel)) {
            repostService.processMessageAttachmentRepostCheck(message);
            List<CachedEmbed> imageEmbeds = message.getEmbeds().stream().filter(messageEmbed -> messageEmbed.getType().equals(EmbedType.IMAGE)).collect(Collectors.toList());
            repostService.processMessageEmbedsRepostCheck(imageEmbeds, message);
        }
    }

    @Override
    public FeatureEnum getFeature() {
        return UtilityFeature.REPOST_DETECTION;
    }

}
