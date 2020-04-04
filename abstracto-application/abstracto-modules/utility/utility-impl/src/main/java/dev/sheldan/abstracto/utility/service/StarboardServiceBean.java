package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.service.management.EmoteManagementService;
import dev.sheldan.abstracto.core.service.management.PostTargetManagement;
import dev.sheldan.abstracto.core.service.management.UserManagementService;
import dev.sheldan.abstracto.core.models.AServerChannelMessage;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.models.MessageToSend;
import dev.sheldan.abstracto.core.service.Bot;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.templating.TemplateService;
import dev.sheldan.abstracto.utility.config.StarboardConfig;
import dev.sheldan.abstracto.utility.models.StarboardPost;
import dev.sheldan.abstracto.utility.models.template.starboard.StarStatsModel;
import dev.sheldan.abstracto.utility.models.template.starboard.StarStatsPost;
import dev.sheldan.abstracto.utility.models.template.starboard.StarStatsUser;
import dev.sheldan.abstracto.utility.models.template.starboard.StarboardPostModel;
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
    private EmoteManagementService emoteManagementService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private StarboardPostManagementService starboardPostManagementService;

    @Autowired
    private StarboardConfig starboardConfig;

    @Autowired
    private UserManagementService userManagementService;

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
        postTargetService.sendEmbedInPostTarget(messageToSend, STARBOARD_POSTTARGET, message.getServerId()).thenAccept(message1 -> {
            AServerChannelMessage aServerChannelMessage = AServerChannelMessage
                    .builder()
                    .messageId(message1.getIdLong())
                    .channel(starboard.getChannelReference())
                    .server(userReacting.getServerReference())
                    .build();
            StarboardPost starboardPost = starboardPostManagementService.createStarboardPost(message, starredUser, userReacting, aServerChannelMessage);
            // TODO maybe in bulk, but numbers should be small enough
            userExceptAuthor.forEach(user -> {
                starboardPostReactorManagementService.addReactor(starboardPost, user);
            });
        });

    }

    private StarboardPostModel buildStarboardPostModel(CachedMessage message, Integer starCount)  {
        Member member = bot.getMemberInServer(message.getServerId(), message.getAuthorId());
        Optional<TextChannel> channel = bot.getTextChannelFromServer(message.getServerId(), message.getChannelId());
        Optional<Guild> guild = bot.getGuildById(message.getServerId());
        AChannel aChannel = AChannel.builder().id(message.getChannelId()).build();
        AUser user = AUser.builder().id(message.getAuthorId()).build();
        AServer server = AServer.builder().id(message.getServerId()).build();
        Optional<AEmote> appropriateEmoteOptional = getAppropriateEmote(message.getServerId(), starCount);
        String emoteText;
        if(appropriateEmoteOptional.isPresent()) {
            AEmote emote = appropriateEmoteOptional.get();
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
    public void updateStarboardPost(StarboardPost post, CachedMessage message, List<AUser> userExceptAuthor)  {
        StarboardPostModel starboardPostModel = buildStarboardPostModel(message, userExceptAuthor.size());
        MessageToSend messageToSend = templateService.renderEmbedTemplate("starboard_post", starboardPostModel);
        CompletableFuture<Message> future = new CompletableFuture<>();
        postTargetService.editOrCreatedInPostTarget(post.getStarboardMessageId(), messageToSend, "starboard", message.getServerId(), future);
        future.thenAccept(newOrOldMessage -> {
            starboardPostManagementService.setStarboardPostMessageId(post, newOrOldMessage.getIdLong());
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
        List<StarStatsPost> starStatsPosts = starboardPosts.stream().map(StarStatsPost::fromStarboardPost).collect(Collectors.toList());
        List<StarStatsUser> topStarReceiver = starboardPostReactorManagementService.retrieveTopStarReceiver(serverId, count);
        Integer postCount = starboardPostManagementService.getPostCount(serverId);
        Integer reactionCount = starboardPostReactorManagementService.getStarCount(serverId);
        List<String> emotes = new ArrayList<>();
        for (int i = 1; i < count + 1; i++) {
            Optional<AEmote> starboardRankingEmote = getStarboardRankingEmote(serverId, i);
            AEmote emote = starboardRankingEmote.orElse(null);
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

    private Optional<AEmote> getStarboardRankingEmote(Long serverId, Integer position) {
        return emoteManagementService.loadEmoteByName("starboardBadge" + position, serverId);
    }

    private Optional<AEmote> getAppropriateEmote(Long serverId, Integer starCount) {
        for(int i = starboardConfig.getLvl().size(); i > 0; i--) {
            Double starMinimum = configService.getDoubleValue("starLvl" + i, serverId);
            if(starCount >= starMinimum) {
                return emoteManagementService.loadEmoteByName("star" + i, serverId);
            }
        }
        return emoteManagementService.loadEmoteByName("star0", serverId);
    }
}
