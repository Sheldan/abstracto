package dev.sheldan.abstracto.listener;

import dev.sheldan.abstracto.core.management.ChannelManagementService;
import dev.sheldan.abstracto.core.management.ServerManagementService;
import dev.sheldan.abstracto.core.management.UserManagementService;
import dev.sheldan.abstracto.core.models.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.listener.MessageEmbeddedModel;
import dev.sheldan.abstracto.core.service.Bot;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.core.models.embed.MessageToSend;
import dev.sheldan.abstracto.templating.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class MessageEmbedListener extends ListenerAdapter {

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

    @Override
    @Transactional
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        String messageRaw = event.getMessage().getContentRaw();
        Matcher matcher = messageRegex.matcher(messageRaw);
        boolean matched = false;
        while(matcher.find()) {
            matched = true;
            String serverId = matcher.group("server");
            String channelId = matcher.group("channel");
            String messageId = matcher.group("message");
            String wholeLink = matcher.group("whole");
            if(event.getMessage().getGuild().getId().equals(serverId)) {
                Long serverIdLong = Long.parseLong(serverId);
                Long channelIdLong = Long.parseLong(channelId);
                Long messageIdLong = Long.parseLong(messageId);
                messageRaw = messageRaw.replace(wholeLink, "");
                messageCache.getMessageFromCache(serverIdLong, channelIdLong, messageIdLong).thenAccept(cachedMessage -> {
                    self.createEmbedAndPostEmbed(event, cachedMessage);
                });

            }
        }
        if(StringUtils.isBlank(messageRaw) && matched) {
            event.getMessage().delete().queue();
        }
    }

    @NotNull
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createEmbedAndPostEmbed(@Nonnull GuildMessageReceivedEvent event, CachedMessage message) {
        MessageEmbeddedModel messageEmbeddedModel = buildTemplateParameter(event, message);
        MessageToSend embed = templateService.renderEmbedTemplate(MESSAGE_EMBED_TEMPLATE, messageEmbeddedModel);
        if(StringUtils.isBlank(embed.getMessage())) {
            event.getChannel().sendMessage(embed.getEmbed()).queue();
        } else {
            event.getChannel().sendMessage(embed.getMessage()).embed(embed.getEmbed()).queue();
        }
    }

    private MessageEmbeddedModel buildTemplateParameter(GuildMessageReceivedEvent event, CachedMessage embeddedMessage) {
        AChannel channel = channelManagementService.loadChannel(event.getChannel().getIdLong());
        AServer server = serverManagementService.loadServer(event.getGuild().getIdLong());
        AUserInAServer user = userManagementService.loadUser(event.getMember());
        Member author = bot.getMemberInServer(embeddedMessage.getServerId(), embeddedMessage.getAuthorId());
        return MessageEmbeddedModel
                .builder()
                .channel(channel)
                .server(server)
                .member(event.getMember())
                .aUserInAServer(user)
                .author(author)
                .sourceChannel(event.getChannel())
                .embeddingUser(event.getMember())
                .user(user.getUserReference())
                .textChannel(event.getChannel())
                .guild(event.getGuild())
                .embeddedMessage(embeddedMessage)
                .build();
    }
}
