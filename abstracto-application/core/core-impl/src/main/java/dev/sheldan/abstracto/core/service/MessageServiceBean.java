package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.exception.ConfiguredEmoteNotUsableException;
import dev.sheldan.abstracto.core.exception.EmoteNotInServerException;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.EmoteManagementService;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class MessageServiceBean implements MessageService {

    @Autowired
    private BotService botService;

    @Autowired
    private EmoteManagementService emoteManagementService;

    @Autowired
    private EmoteService emoteService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private MessageServiceBean self;

    @Autowired
    private TemplateService templateService;

    @Override
    public void addReactionToMessage(String emoteKey, Long serverId, Message message) {
        addReactionToMessageWithFuture(emoteKey, serverId, message);
    }

    @Override
    public CompletableFuture<Void> addReactionToMessageWithFuture(String emoteKey, Long serverId, Message message) {
        Guild guild = botService.getGuildById(serverId);
        return addReactionToMessageWithFuture(emoteKey, guild, message);
    }

    @Override
    public CompletableFuture<Void> addReactionToMessageWithFuture(String emoteKey, Guild guild, Message message) {
        AEmote emote = emoteService.getEmoteOrDefaultEmote(emoteKey, guild.getIdLong());
        return addReactionToMessageWithFuture(emote, guild, message);
    }

    @Override
    public CompletableFuture<Void> addReactionToMessageWithFuture(AEmote emote, Long serverId, Message message) {
        if(Boolean.TRUE.equals(emote.getCustom())) {
           return addReactionToMessageWithFuture(emote.getEmoteId(), serverId, message);
        } else {
            return message.addReaction(emote.getEmoteKey()).submit();
        }
    }

    @Override
    public CompletableFuture<Void> addReactionToMessageWithFuture(AEmote emote, Guild guild, Message message) {
        if(Boolean.TRUE.equals(emote.getCustom())) {
            Emote emoteById = botService.getInstance().getEmoteById(emote.getEmoteId());
            if(emoteById != null) {
                log.trace("Adding custom emote {} as reaction to message {}.", emoteById.getId(), message.getId());
                return message.addReaction(emoteById).submit();
            } else {
                log.error("Emote with key {} and id {} for guild {} was not found.", emote.getName() , emote.getEmoteId(), guild.getId());
                throw new ConfiguredEmoteNotUsableException(emote);
            }
        } else {
            log.trace("Adding default emote {} as reaction to message {}.", emote.getEmoteKey(), message.getId());
            return message.addReaction(emote.getEmoteKey()).submit();
        }
    }

    @Override
    public CompletableFuture<Void> addReactionToMessageWithFuture(Long emoteId, Long serverId, Message message) {
        Emote emoteById = botService.getInstance().getEmoteById(emoteId);
        if(emoteById == null) {
            throw new EmoteNotInServerException(emoteId);
        }
        return message.addReaction(emoteById).submit();
    }

    @Override
    public CompletableFuture<Void> removeReactionFromMessageWithFuture(AEmote emote, Message message) {
        if(Boolean.TRUE.equals(emote.getCustom())) {
            Emote emoteById = botService.getInstance().getEmoteById(emote.getEmoteId());
            if(emoteById == null) {
                throw new EmoteNotInServerException(emote.getEmoteId());
            }
            log.trace("Removing single custom reaction for emote {} on message {}.", emoteById.getId(), message.getId());
            return message.removeReaction(emoteById).submit();
        } else {
            log.trace("Removing single default emote {} reaction from message {}.", emote.getEmoteKey(), message.getId());
            return message.removeReaction(emote.getEmoteKey()).submit();
        }
    }

    @Override
    public CompletableFuture<Void> clearReactionFromMessageWithFuture(AEmote emote, Message message) {
        if(Boolean.TRUE.equals(emote.getCustom())) {
            Emote emoteById = botService.getInstance().getEmoteById(emote.getEmoteId());
            if(emoteById == null) {
                throw new EmoteNotInServerException(emote.getEmoteId());
            }
            log.trace("Clearing reactions for custom emote {} on message {}.", emoteById.getId(), message.getId());
            return message.clearReactions(emoteById).submit();
        } else {
            log.trace("Clearing reactions for default emote {} on message {}.", emote.getEmoteKey(), message.getId());
            return message.clearReactions(emote.getEmoteKey()).submit();
        }
    }

    @Override
    public CompletableFuture<Void> removeReactionFromMessageWithFuture(Integer emoteId, Message message) {
        AEmote emote = emoteManagementService.loadEmote(emoteId);
        return removeReactionFromMessageWithFuture(emote, message);
    }

    @Override
    public CompletableFuture<Void> clearReactionFromMessageWithFuture(Integer emoteId, Message message) {
        AEmote emote = emoteManagementService.loadEmote(emoteId);
        return clearReactionFromMessageWithFuture(emote, message);
    }

    @Override
    public CompletableFuture<Void> removeReactionFromMessageWithFuture(AEmote emote, Long serverId, Long channelId, Long messageId) {
        TextChannel channel = botService.getTextChannelFromServer(serverId, channelId);
        Integer emoteId = emote.getId();
        return channel.retrieveMessageById(messageId).submit()
                .thenCompose(message -> self.removeReactionFromMessageWithFuture(emoteId, message));
    }

    @Override
    public CompletableFuture<Void> removeReactionOfUserFromMessageWithFuture(AEmote emote, Long serverId, Long channelId, Long messageId, Long userId) {
        Guild guild = botService.getGuildById(serverId);
        Integer emoteId = emote.getId();
        TextChannel textChannel = botService.getTextChannelFromServer(serverId, channelId);
        CompletableFuture<Member> memberFuture = guild.retrieveMemberById(userId).submit();
        CompletableFuture<Message> messageFuture = textChannel.retrieveMessageById(messageId).submit();

        return CompletableFuture.allOf(memberFuture, messageFuture).thenCompose(aVoid ->
            memberFuture.thenCompose(member ->
                    self.removeReactionOfUserFromMessageWithFuture(emoteId, messageFuture.join(), memberFuture.join())
            )
        );
    }

    @Override
    public CompletableFuture<Void> removeReactionOfUserFromMessageWithFuture(AEmote emote, Long serverId, Long channelId, Long messageId, Member member) {
        TextChannel channel = botService.getTextChannelFromServer(serverId, channelId);
        Integer emoteId = emote.getId();
        return channel.retrieveMessageById(messageId).submit()
                .thenCompose(message -> self.removeReactionOfUserFromMessageWithFuture(emoteId, message, member));
    }

    @Override
    public CompletableFuture<Void> removeReactionOfUserFromMessageWithFuture(AEmote emote, Message message, Member member) {
        if(Boolean.TRUE.equals(emote.getCustom())) {
            Emote emoteById = botService.getInstance().getEmoteById(emote.getEmoteId());
            if(emoteById == null) {
                throw new EmoteNotInServerException(emote.getEmoteId());
            }
            log.trace("Removing reaction for custom emote {} from user {} on message {}.", emoteById.getId(), member.getId(), member.getId());
            return message.removeReaction(emoteById, member.getUser()).submit();
        } else {
            log.trace("Removing reaction for default emote {} from user {} on message {}.", emote.getEmoteKey(), member.getId(), member.getId());
            return message.removeReaction(emote.getEmoteKey(), member.getUser()).submit();
        }
    }

    @Override
    @Transactional
    public CompletableFuture<Void> removeReactionOfUserFromMessageWithFuture(Integer emoteId, Message message, Member member) {
        AEmote emote = emoteManagementService.loadEmote(emoteId);
        return removeReactionOfUserFromMessageWithFuture(emote, message, member);
    }

    @Override
    public CompletableFuture<Void> removeReactionOfUserFromMessageWithFuture(AEmote emote, Message message, Long userId) {
        Integer emoteId = emote.getId();
        return message.getGuild().retrieveMemberById(userId).submit().thenCompose(member ->
            self.removeReactionOfUserFromMessageWithFuture(emoteId, message, member)
        );
    }

    @Override
    public CompletableFuture<Void> removeReactionOfUserFromMessageWithFuture(Integer emoteId, Message message, Long userId) {
        return message.getGuild().retrieveMemberById(userId).submit().thenCompose(member ->
            self.removeReactionOfUserFromMessageWithFuture(emoteId, message, member)
        );
    }

    @Override
    public CompletableFuture<Void> clearReactionFromMessageWithFuture(AEmote emote, Long serverId, Long channelId, Long messageId) {
        TextChannel channel = botService.getTextChannelFromServer(serverId, channelId);
        Integer emoteId = emote.getId();
        return channel.retrieveMessageById(messageId).submit()
                .thenCompose(message1 -> clearReactionFromMessageWithFuture(emoteId, message1));
    }

    @Override
    public List<CompletableFuture<Void>> addReactionsToMessageWithFuture(List<String> emoteKeys, Long serverId, Message message) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        emoteKeys.forEach(s -> futures.add(addReactionToMessageWithFuture(s, serverId, message)));
        return futures;
    }


    @Override
    public CompletableFuture<Void> deleteMessageInChannelInServer(Long serverId, Long channelId, Long messageId) {
        return botService.deleteMessage(serverId, channelId, messageId);
    }

    @Override
    public CompletableFuture<Message> createStatusMessage(MessageToSend messageToSend, AChannel channel) {
        return channelService.sendMessageToSendToAChannel(messageToSend, channel).get(0);
    }

    @Override
    public CompletableFuture<Message> createStatusMessage(MessageToSend messageToSend, MessageChannel channel) {
        return channelService.sendMessageToSendToChannel(messageToSend, channel).get(0);
    }

    @Override
    public CompletableFuture<Long> createStatusMessageId(MessageToSend messageToSend, MessageChannel channel) {
        return channelService.sendMessageToSendToChannel(messageToSend, channel).get(0).thenApply(ISnowflake::getIdLong);
    }

    @Override
    public void updateStatusMessage(AChannel channel, Long messageId, MessageToSend messageToSend) {
        channelService.editMessageInAChannel(messageToSend, channel, messageId);
    }

    @Override
    public void updateStatusMessage(MessageChannel channel, Long messageId, MessageToSend messageToSend) {
        channelService.editMessageInAChannel(messageToSend, channel, messageId);
    }

    @Override
    public CompletableFuture<Message> sendMessageToUser(AUserInAServer userInAServer, String text) {
        return botService.getMemberInServerAsync(userInAServer).thenCompose(member ->
            sendMessageToUser(member.getUser(), text)
        );
    }

    @Override
    public CompletableFuture<Message> sendTemplateToUser(User user, String template, Object model) {
        String message = templateService.renderTemplate(template, model);
        return sendMessageToUser(user, message);
    }

    @Override
    public CompletableFuture<Void> sendEmbedToUser(User user, String template, Object model) {
        return user.openPrivateChannel().submit().thenCompose(privateChannel ->
                FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInChannel(template, model, privateChannel)));
    }

    @Override
    public CompletableFuture<Message> sendEmbedToUserWithMessage(User user, String template, Object model) {
        log.trace("Sending direct message with template {} to user {}.", template, user.getIdLong());
        return user.openPrivateChannel().submit().thenCompose(privateChannel ->
                channelService.sendEmbedTemplateInChannel(template, model, privateChannel).get(0));
    }

    @Override
    public CompletableFuture<Message> sendMessageToSendToUser(User user, MessageToSend messageToSend) {
        return user.openPrivateChannel().submit().thenCompose(privateChannel -> channelService.sendMessageToSendToChannel(messageToSend, privateChannel).get(0));
    }

    @Override
    public CompletableFuture<Message> sendMessageToUser(User user, String text) {
        log.trace("Sending direct string message to user {}.", user.getIdLong());
        return user.openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage(text)).submit();
    }

    @Override
    public CompletableFuture<Void> deleteMessageInChannelWithUser(User user, Long messageId) {
        log.info("Deleting message {} in channel with user {}.", messageId, user.getIdLong());
        return user.openPrivateChannel().flatMap(privateChannel -> privateChannel.deleteMessageById(messageId)).submit();
    }

    @Override
    public CompletableFuture<Void> editMessageInDMChannel(User user, MessageToSend messageToSend, Long messageId) {
        return user.openPrivateChannel().submit().thenCompose(privateChannel -> channelService.editMessageInAChannelFuture(messageToSend, privateChannel, messageId).thenApply(message -> null));
    }
}
