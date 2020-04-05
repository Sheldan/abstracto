package dev.sheldan.abstracto.utility.listener;

import dev.sheldan.abstracto.core.listener.MessageReceivedListener;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserManagementService;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.listener.models.MessageEmbeddedModel;
import dev.sheldan.abstracto.core.service.Bot;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.core.models.MessageToSend;
import dev.sheldan.abstracto.templating.TemplateService;
import dev.sheldan.abstracto.utility.config.UtilityFeatures;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class MessageEmbedListener implements MessageReceivedListener {

    @Autowired
    private MessageCache messageCache;

    public static final String MESSAGE_EMBED_TEMPLATE = "message";

    private Pattern messageRegex = Pattern.compile("(?<whole>https://discordapp.com/channels/(?<server>\\d+)/(?<channel>\\d+)/(?<message>\\d+)(?:.*?))+");

    @Autowired
    private MessageEmbedListener self;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private Bot bot;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createEmbedAndPostEmbed(@Nonnull Message postedMessage, CachedMessage message) {
        MessageEmbeddedModel messageEmbeddedModel = buildTemplateParameter(postedMessage, message);
        MessageToSend embed = templateService.renderEmbedTemplate(MESSAGE_EMBED_TEMPLATE, messageEmbeddedModel);
        channelService.sendMessageToEndInTextChannel(embed, postedMessage.getTextChannel());
    }

    private MessageEmbeddedModel buildTemplateParameter(Message message, CachedMessage embeddedMessage) {
        AChannel channel = channelManagementService.loadChannel(message.getChannel().getIdLong());
        AServer server = serverManagementService.loadOrCreate(message.getGuild().getIdLong());
        AUserInAServer user = userManagementService.loadUser(message.getMember());
        Member author = bot.getMemberInServer(embeddedMessage.getServerId(), embeddedMessage.getAuthorId());
        TextChannel sourceChannel = bot.getTextChannelFromServer(embeddedMessage.getServerId(), embeddedMessage.getChannelId()).get();
        return MessageEmbeddedModel
                .builder()
                .channel(channel)
                .server(server)
                .member(message.getMember())
                .aUserInAServer(user)
                .author(author)
                .sourceChannel(sourceChannel)
                .embeddingUser(message.getMember())
                .user(user.getUserReference())
                .messageChannel(message.getChannel())
                .guild(message.getGuild())
                .embeddedMessage(embeddedMessage)
                .build();
    }

    @Override
    public void execute(Message message) {
        String messageRaw = message.getContentRaw();
        Matcher matcher = messageRegex.matcher(messageRaw);
        boolean matched = false;
        while(matcher.find()) {
            matched = true;
            String serverId = matcher.group("server");
            String channelId = matcher.group("channel");
            String messageId = matcher.group("message");
            String wholeLink = matcher.group("whole");
            if(message.getGuild().getId().equals(serverId)) {
                Long serverIdLong = Long.parseLong(serverId);
                Long channelIdLong = Long.parseLong(channelId);
                Long messageIdLong = Long.parseLong(messageId);
                messageRaw = messageRaw.replace(wholeLink, "");
                messageCache.getMessageFromCache(serverIdLong, channelIdLong, messageIdLong).thenAccept(cachedMessage -> {
                    self.createEmbedAndPostEmbed(message, cachedMessage);
                });

            }
        }
        if(StringUtils.isBlank(messageRaw) && matched) {
            message.delete().queue();
        }
    }

    @Override
    public String getFeature() {
        return UtilityFeatures.LINK_EMBEDS;
    }
}
