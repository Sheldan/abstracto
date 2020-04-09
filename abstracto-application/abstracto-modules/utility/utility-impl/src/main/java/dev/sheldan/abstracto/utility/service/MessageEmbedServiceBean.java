package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.command.service.UserService;
import dev.sheldan.abstracto.core.converter.UserInServerModelConverter;
import dev.sheldan.abstracto.core.models.dto.UserInServerDto;
import dev.sheldan.abstracto.core.models.template.ChannelModel;
import dev.sheldan.abstracto.core.models.template.ServerModel;
import dev.sheldan.abstracto.core.models.template.UserInServerModel;
import dev.sheldan.abstracto.core.models.template.listener.MessageEmbeddedModel;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.service.Bot;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.utility.models.MessageEmbedLink;
import dev.sheldan.abstracto.utility.service.management.MessageEmbedPostManagementServiceBean;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class MessageEmbedServiceBean implements MessageEmbedService {

    private Pattern messageRegex = Pattern.compile("(?<whole>https://discordapp.com/channels/(?<server>\\d+)/(?<channel>\\d+)/(?<message>\\d+)(?:.*?))+");

    public static final String MESSAGE_EMBED_TEMPLATE = "message";
    public static final String REMOVAL_EMOTE = "removeEmbed";

    @Autowired
    private ChannelService channelManagementService;

    @Autowired
    private UserService userManagementService;

    @Autowired
    private Bot bot;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private MessageEmbedService self;

    @Autowired
    private MessageCache messageCache;

    @Autowired
    private MessageEmbedPostManagementServiceBean messageEmbedPostManagementService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserInServerModelConverter userInServerModelConverter;

    @Autowired
    private UserService userService;

    @Override
    public List<MessageEmbedLink> getLinksInMessage(String message) {
        List<MessageEmbedLink> links = new ArrayList<>();
        Matcher matcher = messageRegex.matcher(message);
        while(matcher.find()) {
            String serverId = matcher.group("server");
            String channelId = matcher.group("channel");
            String messageId = matcher.group("message");
            String wholeLink = matcher.group("whole");
            Long serverIdLong = Long.parseLong(serverId);
            Long channelIdLong = Long.parseLong(channelId);
            Long messageIdLong = Long.parseLong(messageId);
            MessageEmbedLink messageEmbedLink = MessageEmbedLink
                    .builder()
                    .serverId(serverIdLong)
                    .channelId(channelIdLong)
                    .messageId(messageIdLong)
                    .wholeUrl(wholeLink)
                    .build();
            links.add(messageEmbedLink);
        }
        return links;
    }

    @Override
    public void embedLinks(List<MessageEmbedLink> linksToEmbed, TextChannel target, UserInServerDto reason, Message embeddingMessage) {
        linksToEmbed.forEach(messageEmbedLink -> {
            messageCache.getMessageFromCache(messageEmbedLink.getServerId(), messageEmbedLink.getChannelId(), messageEmbedLink.getMessageId())
                    .thenAccept(cachedMessage -> {
                        self.embedLink(cachedMessage, target, reason, embeddingMessage);
                    });
        });
    }

    @Override
    @Transactional
    public void embedLink(CachedMessage cachedMessage, TextChannel target, UserInServerDto cause, Message embeddingMessage) {
        MessageEmbeddedModel messageEmbeddedModel = buildTemplateParameter(embeddingMessage, cachedMessage);
        MessageToSend embed = templateService.renderEmbedTemplate(MESSAGE_EMBED_TEMPLATE, messageEmbeddedModel);
        List<CompletableFuture<Message>> completableFutures = channelService.sendMessageToEndInTextChannel(embed, target);

        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).thenAccept(aVoid -> {
            try {
                Message createdMessage = completableFutures.get(0).get();
                messageEmbedPostManagementService.createMessageEmbed(cachedMessage, createdMessage, cause);
                messageService.addReactionToMessage(REMOVAL_EMOTE, cachedMessage.getServerId(), createdMessage);
            } catch (InterruptedException | ExecutionException e) {
                log.error("Failed to post message embed.", e);
            }
        });

    }

    private MessageEmbeddedModel buildTemplateParameter(Message message, CachedMessage embeddedMessage) {
        ChannelModel currentChannel = ChannelModel.builder().id(message.getTextChannel().getIdLong()).build();
        Guild guild = message.getGuild();
        ServerModel server = ServerModel.builder().id(guild.getIdLong()).build();
        UserInServerModel embeddingUser = userInServerModelConverter.fromMember(message.getMember());
        UserInServerModel embeddedUser = userInServerModelConverter.fromUser(userService.loadUser(embeddedMessage.getServerId(), embeddedMessage.getAuthorId()));
        ServerModel sourceServer = ServerModel.builder().id(embeddedMessage.getServerId()).build();
        return MessageEmbeddedModel
                .builder()
                .channel(currentChannel)
                .server(server)
                .embeddedMessage(embeddedMessage)
                .embeddedUser(embeddedUser.getUser())
                .embeddedUserInServer(embeddedUser)
                .userModel(embeddingUser.getUser())
                .userInServer(embeddedUser)
                .sourceServer(sourceServer)
                .build();
    }
}
