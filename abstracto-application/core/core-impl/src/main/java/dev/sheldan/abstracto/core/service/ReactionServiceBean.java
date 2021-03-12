package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.exception.ConfiguredEmoteNotUsableException;
import dev.sheldan.abstracto.core.exception.EmoteNotInServerException;
import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.cache.CachedEmote;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReaction;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.service.management.EmoteManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.core.config.MetricConstants.DISCORD_API_INTERACTION_METRIC;
import static dev.sheldan.abstracto.core.config.MetricConstants.INTERACTION_TYPE;

@Component
@Slf4j
public class ReactionServiceBean implements ReactionService {

    @Autowired
    private MessageService messageService;

    @Autowired
    private EmoteService emoteService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private GuildService guildService;

    @Autowired
    private BotService botService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private EmoteManagementService emoteManagementService;

    @Autowired
    private ReactionService self;

    @Autowired
    private MetricService metricService;

    public static final CounterMetric REACTION_ADDED_METRIC = CounterMetric
            .builder()
            .name(DISCORD_API_INTERACTION_METRIC)
            .tagList(Arrays.asList(MetricTag.getTag(INTERACTION_TYPE, "reaction.added")))
            .build();

    public static final CounterMetric REACTION_REMOVED_METRIC = CounterMetric
            .builder()
            .name(DISCORD_API_INTERACTION_METRIC)
            .tagList(Arrays.asList(MetricTag.getTag(INTERACTION_TYPE, "reaction.removed")))
            .build();

    public static final CounterMetric REACTION_CLEARED_METRIC = CounterMetric
            .builder()
            .name(DISCORD_API_INTERACTION_METRIC)
            .tagList(Arrays.asList(MetricTag.getTag(INTERACTION_TYPE, "reaction.cleared")))
            .build();

    @Override
    public CompletableFuture<Void> removeReactionFromMessage(CachedReaction reaction, CachedMessage cachedMessage) {
        return messageService.loadMessageFromCachedMessage(cachedMessage).thenCompose(message -> removeReactionFromMessage(reaction, message));
    }

    @Override
    public CompletableFuture<Void> removeReactionFromMessage(CachedReaction reaction, Message message) {
        return memberService.retrieveUserById(reaction.getUser().getUserId()).thenCompose(user -> {
            if(reaction.getEmote().getCustom()) {
                return emoteService.getEmoteFromCachedEmote(reaction.getEmote()).thenCompose(emote ->
                        removeReaction(message, emote, user)
                );
            } else {
                return removeReaction(message, reaction.getEmote().getEmoteName(), user);
            }
        });
    }

    @Override
    public void addReactionToMessage(String emoteKey, Long serverId, Message message) {
        addReactionToMessageAsync(emoteKey, serverId, message);
    }

    @Override
    public void addDefaultReactionToMessage(String unicode, Message message) {
        addDefaultReactionToMessageAsync(unicode, message);
    }

    @Override
    public CompletableFuture<Void> addDefaultReactionToMessageAsync(String unicode, Message message) {
        metricService.incrementCounter(REACTION_ADDED_METRIC);
        return message.addReaction(unicode).submit();
    }

    @Override
    public CompletableFuture<Void> addDefaultReactionToMessageAsync(String unicode, Long serverId, Long channelId, Long messageId) {
        return channelService.retrieveMessageInChannel(serverId, channelId, messageId)
                .thenCompose(message -> self.addDefaultReactionToMessageAsync(unicode, message));
    }

    @Override
    public CompletableFuture<Void> addReactionToMessageAsync(String emoteKey, Long serverId, Message message) {
        Guild guild = guildService.getGuildById(serverId);
        return addReactionToMessageAsync(emoteKey, guild, message);
    }

    @Override
    public CompletableFuture<Void> addReactionToMessageAsync(String emoteKey, Guild guild, Message message) {
        AEmote emote = emoteService.getEmoteOrDefaultEmote(emoteKey, guild.getIdLong());
        return addReactionToMessageAsync(emote, guild, message);
    }

    @Override
    public CompletableFuture<Void> addReactionToMessageAsync(AEmote emote, Long serverId, Message message) {
        if(Boolean.TRUE.equals(emote.getCustom())) {
            return addReactionToMessageAsync(emote.getEmoteId(), serverId, message);
        } else {
            return addDefaultReactionToMessageAsync(emote.getEmoteKey(), message);
        }
    }

    @Override
    public CompletableFuture<Void> addReactionToMessageAsync(AEmote emote, Guild guild, Message message) {
        if(Boolean.TRUE.equals(emote.getCustom())) {
            Emote emoteById = botService.getInstance().getEmoteById(emote.getEmoteId());
            if(emoteById != null) {
                log.trace("Adding custom emote {} as reaction to message {}.", emoteById.getId(), message.getId());
                return addReactionToMessageAsync(emoteById, message);
            } else {
                log.error("Emote with key {} and id {} for guild {} was not found.", emote.getName() , emote.getEmoteId(), guild.getId());
                throw new ConfiguredEmoteNotUsableException(emote);
            }
        } else {
            log.trace("Adding default emote {} as reaction to message {}.", emote.getEmoteKey(), message.getId());
            return addDefaultReactionToMessageAsync(emote.getEmoteKey(), message);
        }
    }

    @Override
    public CompletableFuture<Void> addReactionToMessageAsync(Emote emote, Message message) {
        metricService.incrementCounter(REACTION_ADDED_METRIC);
        return message.addReaction(emote).submit();
    }

    @Override
    public CompletableFuture<Void> addReactionToMessageAsync(Long emoteId, Long serverId, Message message) {
        Emote emoteById = botService.getInstance().getEmoteById(emoteId);
        if(emoteById == null) {
            throw new EmoteNotInServerException(emoteId);
        }
        return addReactionToMessageAsync(emoteById, message);
    }

    @Override
    public CompletableFuture<Void> addReactionToMessageAsync(String emoteKey, Long serverId, Long channelId, Long messageId) {
        return channelService.retrieveMessageInChannel(serverId, channelId, messageId)
                .thenCompose(message -> self.addReactionToMessageAsync(emoteKey, serverId, message));
    }

    @Override
    public CompletableFuture<Void> removeReactionFromMessageWithFuture(AEmote emote, Message message) {
        if(Boolean.TRUE.equals(emote.getCustom())) {
            Emote emoteById = botService.getInstance().getEmoteById(emote.getEmoteId());
            if(emoteById == null) {
                throw new EmoteNotInServerException(emote.getEmoteId());
            }
            log.trace("Removing single custom reaction for emote {} on message {}.", emoteById.getId(), message.getId());
            return removeReaction(message, emoteById);
        } else {
            log.trace("Removing single default emote {} reaction from message {}.", emote.getEmoteKey(), message.getId());
            return removeReaction(message, emote.getEmoteKey());
        }
    }

    @Override
    public CompletableFuture<Void> removeReaction(Message message, String key) {
        metricService.incrementCounter(REACTION_REMOVED_METRIC);
        return message.removeReaction(key).submit();
    }

    @Override
    public CompletableFuture<Void> removeReaction(Message message, String key, User user) {
        metricService.incrementCounter(REACTION_REMOVED_METRIC);
        return message.removeReaction(key, user).submit();
    }

    @Override
    public CompletableFuture<Void> removeReaction(Message message, Emote emoteById) {
        metricService.incrementCounter(REACTION_REMOVED_METRIC);
        return message.removeReaction(emoteById).submit();
    }

    @Override
    public CompletableFuture<Void> removeReaction(Message message, Emote emoteById, User user) {
        metricService.incrementCounter(REACTION_REMOVED_METRIC);
        return message.removeReaction(emoteById, user).submit();
    }

    @Override
    public CompletableFuture<Void> removeReaction(Message message, CachedEmote cachedEmote, User user) {
        metricService.incrementCounter(REACTION_REMOVED_METRIC);
        String customEmoteAsUnicode = cachedEmote.getEmoteName() + ":" + cachedEmote.getEmoteId();
        return ((TextChannel) message.getChannel()).removeReactionById(message.getId(), customEmoteAsUnicode, user).submit();
    }

    @Override
    public CompletableFuture<Void> removeReaction(CachedMessage message, CachedEmote cachedEmote, ServerUser user) {
        CompletableFuture<Message> messageFuture = messageService.loadMessageFromCachedMessage(message);
        CompletableFuture<Member> memberFuture = memberService.retrieveMemberInServer(user);
        return FutureUtils.toSingleFuture(Arrays.asList(messageFuture, memberFuture)).thenCompose(unused ->
            removeReaction(messageFuture.join(), cachedEmote, memberFuture.join().getUser())
        );
    }

    @Override
    public CompletableFuture<Void> clearReactionFromMessageWithFuture(AEmote emote, Message message) {
        if(Boolean.TRUE.equals(emote.getCustom())) {
            Emote emoteById = botService.getInstance().getEmoteById(emote.getEmoteId());
            if(emoteById == null) {
                throw new EmoteNotInServerException(emote.getEmoteId());
            }
            log.trace("Clearing reactions for custom emote {} on message {}.", emoteById.getId(), message.getId());
            return clearReaction(message, emoteById);
        } else {
            log.trace("Clearing reactions for default emote {} on message {}.", emote.getEmoteKey(), message.getId());
            return clearReaction(message, emote.getEmoteKey());
        }
    }

    public CompletableFuture<Void> clearReaction(Message message, String key) {
        metricService.incrementCounter(REACTION_CLEARED_METRIC);
        return message.clearReactions(key).submit();
    }

    public CompletableFuture<Void> clearReaction(Message message, Emote emoteById) {
        metricService.incrementCounter(REACTION_CLEARED_METRIC);
        return message.clearReactions(emoteById).submit();
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
        Integer emoteId = emote.getId();
        return channelService.retrieveMessageInChannel(serverId, channelId, messageId)
                .thenCompose(message -> self.removeReactionFromMessageWithFuture(emoteId, message));
    }

    @Override
    public CompletableFuture<Void> removeReactionOfUserFromMessageWithFuture(AEmote emote, Long serverId, Long channelId, Long messageId, Long userId) {
        Guild guild = guildService.getGuildById(serverId);
        Integer emoteId = emote.getId();
        CompletableFuture<Member> memberFuture = guild.retrieveMemberById(userId).submit();
        CompletableFuture<Message> messageFuture = channelService.retrieveMessageInChannel(serverId, channelId, messageId);

        return CompletableFuture.allOf(memberFuture, messageFuture).thenCompose(aVoid ->
                memberFuture.thenCompose(member ->
                        self.removeReactionOfUserFromMessageWithFuture(emoteId, messageFuture.join(), memberFuture.join())
                )
        );
    }

    @Override
    public CompletableFuture<Void> removeReactionOfUserFromMessageWithFuture(AEmote emote, Long serverId, Long channelId, Long messageId, Member member) {
        Integer emoteId = emote.getId();
        return channelService.retrieveMessageInChannel(serverId, channelId, messageId)
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
            return removeReaction(message, emoteById, member.getUser());
        } else {
            log.trace("Removing reaction for default emote {} from user {} on message {}.", emote.getEmoteKey(), member.getId(), member.getId());
            return removeReaction(message,  emote.getEmoteKey(), member.getUser());
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
        Integer emoteId = emote.getId();
        return channelService.retrieveMessageInChannel(serverId, channelId, messageId)
                .thenCompose(message1 -> clearReactionFromMessageWithFuture(emoteId, message1));
    }

    @Override
    public List<CompletableFuture<Void>> addReactionsToMessageWithFuture(List<String> emoteKeys, Long serverId, Message message) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        emoteKeys.forEach(s -> futures.add(addReactionToMessageAsync(s, serverId, message)));
        return futures;
    }

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(REACTION_ADDED_METRIC, "Reactions added");
        metricService.registerCounter(REACTION_REMOVED_METRIC, "Reactions removed");
        metricService.registerCounter(REACTION_CLEARED_METRIC, "Reactions cleared");
    }
}
