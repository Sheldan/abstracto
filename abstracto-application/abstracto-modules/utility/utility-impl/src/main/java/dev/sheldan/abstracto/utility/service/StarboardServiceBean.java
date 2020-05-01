package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.service.management.PostTargetManagement;
import dev.sheldan.abstracto.core.models.AServerAChannelMessage;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.utility.config.StarboardConfig;
import dev.sheldan.abstracto.utility.models.database.StarboardPost;
import dev.sheldan.abstracto.utility.models.template.commands.starboard.StarStatsModel;
import dev.sheldan.abstracto.utility.models.template.commands.starboard.StarStatsPost;
import dev.sheldan.abstracto.utility.models.template.commands.starboard.StarStatsUser;
import dev.sheldan.abstracto.utility.models.template.commands.starboard.StarboardPostModel;
import dev.sheldan.abstracto.utility.service.management.StarboardPostManagementService;
import dev.sheldan.abstracto.utility.service.management.StarboardPostReactorManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


@Component
@Slf4j
public class StarboardServiceBean implements StarboardService {

    public static final String STARBOARD_POSTTARGET = "starboard";
    public static final String STARBOARD_POST_TEMPLATE = "starboard_post";

    @Autowired
    private BotService botService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private StarboardPostManagementService starboardPostManagementService;

    @Autowired
    private StarboardConfig starboardConfig;

    @Autowired
    private StarboardPostReactorManagementService starboardPostReactorManagementService;

    @Autowired
    private PostTargetManagement postTargetManagement;

    @Autowired
    private EmoteService emoteService;

    @Override
    public void createStarboardPost(CachedMessage message, List<AUser> userExceptAuthor, AUserInAServer userReacting, AUserInAServer starredUser)  {
        StarboardPostModel starboardPostModel = buildStarboardPostModel(message, userExceptAuthor.size());
        MessageToSend messageToSend = templateService.renderEmbedTemplate(STARBOARD_POST_TEMPLATE, starboardPostModel);
        PostTarget starboard = postTargetManagement.getPostTarget(STARBOARD_POSTTARGET, message.getServerId());
        List<CompletableFuture<Message>> completableFutures = postTargetService.sendEmbedInPostTarget(messageToSend, STARBOARD_POSTTARGET, message.getServerId());
        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).thenAccept(aVoid -> {
            try {
                Message message1 = completableFutures.get(0).get();
                AServerAChannelMessage aServerAChannelMessage = AServerAChannelMessage
                        .builder()
                        .messageId(message1.getIdLong())
                        .channel(starboard.getChannelReference())
                        .server(userReacting.getServerReference())
                        .build();
                StarboardPost starboardPost = starboardPostManagementService.createStarboardPost(message, starredUser, userReacting, aServerAChannelMessage);
                userExceptAuthor.forEach(user ->
                    starboardPostReactorManagementService.addReactor(starboardPost, user)
                );
            } catch (InterruptedException | ExecutionException e) {
                log.error("Failed to post messages.", e);
            }
        }) .exceptionally(throwable -> {
            log.error("Failed to create starboard post for message {} in channel {} in server {}", message.getMessageId(), message.getChannelId(), message.getServerId(), throwable);
            return null;
        });

    }

    private StarboardPostModel buildStarboardPostModel(CachedMessage message, Integer starCount)  {
        Member member = botService.getMemberInServer(message.getServerId(), message.getAuthorId());
        Optional<TextChannel> channel = botService.getTextChannelFromServer(message.getServerId(), message.getChannelId());
        Optional<Guild> guild = botService.getGuildById(message.getServerId());
        AChannel aChannel = AChannel.builder().id(message.getChannelId()).build();
        AUser user = AUser.builder().id(message.getAuthorId()).build();
        AServer server = AServer.builder().id(message.getServerId()).build();
        String starLevelEmote = getAppropriateEmote(message.getServerId(), starCount);
        return StarboardPostModel
                .builder()
                .message(message)
                .author(member)
                .channel(channel.orElse(null))
                .aChannel(aChannel)
                .starCount(starCount)
                .guild(guild.orElse(null))
                .user(user)
                .server(server)
                .starLevelEmote(starLevelEmote)
                .build();
    }

    @Override
    public void updateStarboardPost(StarboardPost post, CachedMessage message, List<AUser> userExceptAuthor)  {
        StarboardPostModel starboardPostModel = buildStarboardPostModel(message, userExceptAuthor.size());
        MessageToSend messageToSend = templateService.renderEmbedTemplate(STARBOARD_POST_TEMPLATE, starboardPostModel);
        List<CompletableFuture<Message>> futures = new ArrayList<>();
        postTargetService.editOrCreatedInPostTarget(post.getStarboardMessageId(), messageToSend, STARBOARD_POSTTARGET, message.getServerId(), futures);
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenAccept(aVoid -> {
            try {
                starboardPostManagementService.setStarboardPostMessageId(post, futures.get(0).get().getIdLong());
            } catch (InterruptedException | ExecutionException e) {
                log.error("Failed to post starboard post.", e);
            }
        }).exceptionally(throwable -> {
            log.error("Failed to update starboard post {}.", post.getId(), throwable);
            return null;
        });
    }

    @Override
    public void removeStarboardPost(StarboardPost message)  {
        AChannel starboardChannel = message.getStarboardChannel();
        botService.deleteMessage(starboardChannel.getServer().getId(), starboardChannel.getId(), message.getStarboardMessageId());
    }

    @Override
    public StarStatsModel retrieveStarStats(Long serverId)  {
        int count = 3;
        List<StarboardPost> starboardPosts = starboardPostManagementService.retrieveTopPosts(serverId, count);
        List<StarStatsUser> topStarGivers = starboardPostReactorManagementService.retrieveTopStarGiver(serverId, count);
        List<StarStatsPost> starStatsPosts = starboardPosts.stream().map(StarStatsPost::fromStarboardPost).collect(Collectors.toList());
        List<StarStatsUser> topStarReceiver = starboardPostReactorManagementService.retrieveTopStarReceiver(serverId, count);
        Integer postCount = starboardPostManagementService.getPostCount(serverId);
        Integer reactionCount = starboardPostReactorManagementService.getStarCount(serverId);
        List<String> emotes = new ArrayList<>();
        for (int i = 1; i < count + 1; i++) {
            emotes.add(getStarboardRankingEmote(serverId, i));
        }

        return StarStatsModel
                .builder()
                .badgeEmotes(emotes)
                .starGiver(topStarGivers)
                .starReceiver(topStarReceiver)
                .topPosts(starStatsPosts)
                .starredMessages(postCount)
                .totalStars(reactionCount)
                .build();
    }

    private String getStarboardRankingEmote(Long serverId, Integer position) {
        return emoteService.getUsableEmoteOrDefault(serverId, buildBadgeName(position));
    }

    private String buildBadgeName(Integer position) {
        return "starboardBadge" + position;
    }

    private String getAppropriateEmote(Long serverId, Integer starCount) {
        for(int i = starboardConfig.getLvl().size(); i > 0; i--) {
            Double starMinimum = configService.getDoubleValue("starLvl" + i, serverId);
            if(starCount >= starMinimum) {
                return emoteService.getUsableEmoteOrDefault(serverId, "star" + i);
            }
        }
        return emoteService.getUsableEmoteOrDefault(serverId, "star0");
    }
}
