package dev.sheldan.abstracto.utility.listener.repost;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.listener.MessageReceivedListener;
import dev.sheldan.abstracto.utility.config.features.UtilityFeature;
import dev.sheldan.abstracto.utility.service.RepostCheckChannelService;
import dev.sheldan.abstracto.utility.service.RepostService;
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
public class RepostMessageReceivedListener implements MessageReceivedListener {

    @Autowired
    private RepostCheckChannelService repostCheckChannelService;

    @Autowired
    private RepostService repostService;

    @Override
    public void execute(Message message) {
        if(repostCheckChannelService.duplicateCheckEnabledForChannel(message.getTextChannel())) {
            repostService.processMessageAttachmentRepostCheck(message);
            List<MessageEmbed> imageEmbeds = message.getEmbeds().stream().filter(messageEmbed -> messageEmbed.getType().equals(EmbedType.IMAGE)).collect(Collectors.toList());
            repostService.processMessageEmbedsRepostCheck(imageEmbeds, message);
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
