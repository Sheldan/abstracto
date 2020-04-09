package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.command.service.UserService;
import dev.sheldan.abstracto.core.models.AChannel;
import dev.sheldan.abstracto.core.models.AUserInAServer;
import dev.sheldan.abstracto.core.models.converter.ChannelConverter;
import dev.sheldan.abstracto.core.models.converter.UserInServerConverter;
import dev.sheldan.abstracto.core.models.dto.*;
import dev.sheldan.abstracto.core.models.AServerAChannelMessage;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.service.Bot;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.utility.config.StarboardConfig;
import dev.sheldan.abstracto.utility.converter.StarPostConverter;
import dev.sheldan.abstracto.utility.models.database.StarboardPost;
import dev.sheldan.abstracto.utility.models.template.commands.starboard.StarStatsModel;
import dev.sheldan.abstracto.utility.models.template.commands.starboard.StarStatsPost;
import dev.sheldan.abstracto.utility.models.template.commands.starboard.StarStatsUser;
import dev.sheldan.abstracto.utility.models.template.commands.starboard.StarboardPostModel;
import dev.sheldan.abstracto.utility.service.management.StarboardPostManagementServiceBean;
import dev.sheldan.abstracto.utility.service.management.StarboardPostReactorManagementServiceBean;
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
    private Bot bot;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private StarboardPostManagementServiceBean starboardPostManagementService;

    @Autowired
    private StarboardConfig starboardConfig;

    @Autowired
    private UserService userManagementService;

    @Autowired
    private StarboardPostReactorManagementServiceBean starboardPostReactorManagementService;

    @Autowired
    private PostTargetService postTargetManagement;

    @Autowired
    private EmoteService emoteService;

    @Autowired
    private UserInServerConverter userInServerConverter;

    @Autowired
    private ChannelConverter channelConverter;

    @Autowired
    private StarPostConverter starPostConverter;

    @Override
    public void createStarboardPost(CachedMessage message, List<UserDto> userExceptAuthor, UserInServerDto userReacting, UserInServerDto starredUser)  {
        StarboardPostModel starboardPostModel = buildStarboardPostModel(message, userExceptAuthor.size());
        MessageToSend messageToSend = templateService.renderEmbedTemplate(STARBOARD_POST_TEMPLATE, starboardPostModel);
        PostTargetDto starboard = postTargetManagement.getPostTarget(STARBOARD_POSTTARGET, message.getServerId());
        List<CompletableFuture<Message>> completableFutures = postTargetService.sendEmbedInPostTarget(messageToSend, STARBOARD_POSTTARGET, message.getServerId());
        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).thenAccept(aVoid -> {
            try {
                Message message1 = completableFutures.get(0).get();
                AServerAChannelMessage aServerAChannelMessage = AServerAChannelMessage
                        .builder()
                        .messageId(message1.getIdLong())
                        .channel(starboard.getChannelReference())
                        .server(userReacting.getServer())
                        .build();
                StarboardPost starboardPost = starboardPostManagementService.createStarboardPost(message, starredUser, userReacting, aServerAChannelMessage);
                // TODO maybe in bulk, but numbers should be small enough
                userExceptAuthor.forEach(user -> {
                    starboardPostReactorManagementService.addReactor(starboardPost, user);
                });
            } catch (InterruptedException | ExecutionException e) {
                log.error("Failed to post messages.", e);
            }
        });

    }

    private StarboardPostModel buildStarboardPostModel(CachedMessage message, Integer starCount)  {
        Member member = bot.getMemberInServer(message.getServerId(), message.getAuthorId());
        Optional<TextChannel> channel = bot.getTextChannelFromServer(message.getServerId(), message.getChannelId());
        Optional<Guild> guild = bot.getGuildById(message.getServerId());
        ChannelDto aChannel = ChannelDto.builder().id(message.getChannelId()).build();
        UserDto user = UserDto.builder().id(message.getAuthorId()).build();
        ServerDto server = ServerDto.builder().id(message.getServerId()).build();
        Optional<EmoteDto> appropriateEmoteOptional = getAppropriateEmote(message.getServerId(), starCount);
        String emoteText;
        if(appropriateEmoteOptional.isPresent()) {
            EmoteDto emote = appropriateEmoteOptional.get();
            emoteText = emoteService.getEmoteAsMention(emote, message.getServerId(), "⭐");
        } else  {
            log.warn("No emote defined to be used for starboard post. Falling back to default.");
            emoteText = "⭐";
        }
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
                .starLevelEmote(emoteText)
                .build();
    }

    @Override
    public void updateStarboardPost(StarboardPost post, CachedMessage message, List<UserDto> userExceptAuthor)  {
        StarboardPostModel starboardPostModel = buildStarboardPostModel(message, userExceptAuthor.size());
        MessageToSend messageToSend = templateService.renderEmbedTemplate("starboard_post", starboardPostModel);
        List<CompletableFuture<Message>> futures = new ArrayList<>();
        postTargetService.editOrCreatedInPostTarget(post.getStarboardMessageId(), messageToSend, "starboard", message.getServerId(), futures);
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenAccept(aVoid -> {
            try {
                starboardPostManagementService.setStarboardPostMessageId(post, futures.get(0).get().getIdLong());
            } catch (InterruptedException | ExecutionException e) {
                log.error("Failed to post starboard post.", e);
            }
        });
    }

    @Override
    public void removeStarboardPost(StarboardPost message)  {
        AChannel starboardChannel = message.getStarboardChannel();
        bot.deleteMessage(starboardChannel.getServer().getId(), starboardChannel.getId(), message.getStarboardMessageId());
    }

    @Override
    public StarStatsModel retrieveStarStats(Long serverId)  {
        int count = 3;
        List<StarboardPost> starboardPosts = starboardPostManagementService.retrieveTopPosts(serverId, count);
        List<StarStatsUser> topStarGivers = starboardPostReactorManagementService.retrieveTopStarGiver(serverId, count);
        List<StarStatsPost> starStatsPosts = starboardPosts.stream().map(starboardPost -> starPostConverter.fromStarboardPost(starboardPost)).collect(Collectors.toList());
        List<StarStatsUser> topStarReceiver = starboardPostReactorManagementService.retrieveTopStarReceiver(serverId, count);
        Integer postCount = starboardPostManagementService.getPostCount(serverId);
        Integer reactionCount = starboardPostReactorManagementService.getStarCount(serverId);
        List<String> emotes = new ArrayList<>();
        for (int i = 1; i < count + 1; i++) {
            Optional<EmoteDto> starboardRankingEmote = getStarboardRankingEmote(serverId, i);
            EmoteDto emote = starboardRankingEmote.orElse(null);
            String defaultEmoji = starboardConfig.getBadge().get(i - 1);
            emotes.add(emoteService.getEmoteAsMention(emote, serverId, defaultEmoji));
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

    private Optional<EmoteDto> getStarboardRankingEmote(Long serverId, Integer position) {
        return emoteService.getEmoteByName("starboardBadge" + position, serverId);
    }

    private Optional<EmoteDto> getAppropriateEmote(Long serverId, Integer starCount) {
        for(int i = starboardConfig.getLvl().size(); i > 0; i--) {
            Double starMinimum = configService.getDoubleValue("starLvl" + i, serverId);
            if(starCount >= starMinimum) {
                return emoteService.getEmoteByName("star" + i, serverId);
            }
        }
        return emoteService.getEmoteByName("star0", serverId);
    }
}
