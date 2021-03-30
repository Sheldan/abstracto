package dev.sheldan.abstracto.starboard.service;

import dev.sheldan.abstracto.core.exception.UserInServerNotFoundException;
import dev.sheldan.abstracto.core.models.AServerAChannelMessage;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.database.PostTarget;
import dev.sheldan.abstracto.core.models.property.SystemConfigProperty;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import dev.sheldan.abstracto.core.service.management.PostTargetManagement;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.starboard.config.StarboardFeatureConfig;
import dev.sheldan.abstracto.starboard.config.StarboardPostTarget;
import dev.sheldan.abstracto.starboard.model.database.StarboardPost;
import dev.sheldan.abstracto.starboard.model.template.*;
import dev.sheldan.abstracto.starboard.service.management.StarboardPostManagementService;
import dev.sheldan.abstracto.starboard.service.management.StarboardPostReactorManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;


@Component
@Slf4j
public class StarboardServiceBean implements StarboardService {

    public static final String STARBOARD_POST_TEMPLATE = "starboard_post";

    @Autowired
    private MemberService memberService;

    @Autowired
    private GuildService guildService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private StarboardPostManagementService starboardPostManagementService;

    @Autowired
    private StarboardPostReactorManagementService starboardPostReactorManagementService;

    @Autowired
    private PostTargetManagement postTargetManagement;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private EmoteService emoteService;

    @Autowired
    private DefaultConfigManagementService defaultConfigManagementService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private StarboardServiceBean self;

    @Autowired
    private StarboardPostDeletedListenerManager starboardPostDeletedListenerManager;

    @Autowired
    private StarboardPostCreatedListenerManager starboardPostCreatedListenerManager;

    @Autowired
    private UserService userService;

    @Override
    public CompletableFuture<Void> createStarboardPost(CachedMessage message, List<AUserInAServer> userExceptAuthor, AUserInAServer userReacting, AUserInAServer starredUser)  {
        Long starredUserId = starredUser.getUserInServerId();
        List<Long> userExceptAuthorIds = userExceptAuthor.stream().map(AUserInAServer::getUserInServerId).collect(Collectors.toList());
        Long userReactingId = userReacting.getUserReference().getId();
        return buildStarboardPostModel(message, userExceptAuthor.size()).thenCompose(starboardPostModel ->
            self.sendStarboardPostAndStore(message, starredUserId, userExceptAuthorIds, starboardPostModel, userReactingId)
        );

    }

    @Transactional
    public CompletionStage<Void> sendStarboardPostAndStore(CachedMessage message, Long starredUserId, List<Long> userExceptAuthorIds, StarboardPostModel starboardPostModel, Long userReactingId) {
        MessageToSend messageToSend = templateService.renderEmbedTemplate(STARBOARD_POST_TEMPLATE, starboardPostModel, message.getServerId());
        PostTarget starboard = postTargetManagement.getPostTarget(StarboardPostTarget.STARBOARD.getKey(), message.getServerId());
        List<CompletableFuture<Message>> completableFutures = postTargetService.sendEmbedInPostTarget(messageToSend, StarboardPostTarget.STARBOARD, message.getServerId());
        Long starboardChannelId = starboard.getChannelReference().getId();
        return CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).thenAccept(aVoid ->
            self.persistPost(message, userExceptAuthorIds, completableFutures, starboardChannelId, starredUserId, userReactingId)
        );
    }

    @Transactional
    public void persistPost(CachedMessage message, List<Long> userExceptAuthorIds, List<CompletableFuture<Message>> completableFutures, Long starboardChannelId, Long starredUserId, Long userReactingId) {
        AUserInAServer innerStarredUser = userInServerManagementService.loadUserOptional(starredUserId).orElseThrow(() -> new UserInServerNotFoundException(starredUserId));
        AChannel starboardChannel = channelManagementService.loadChannel(starboardChannelId);
        Message starboardMessage = completableFutures.get(0).join();
        AServerAChannelMessage aServerAChannelMessage = AServerAChannelMessage
                .builder()
                .messageId(starboardMessage.getIdLong())
                .channel(starboardChannel)
                .server(starboardChannel.getServer())
                .build();
        StarboardPost starboardPost = starboardPostManagementService.createStarboardPost(message, innerStarredUser, aServerAChannelMessage);
        log.info("Persisting starboard post in channel {} with message {} with {} reactors.", starboardMessage.getId(),starboardChannelId, userExceptAuthorIds.size());
        if(userExceptAuthorIds.isEmpty()) {
            log.warn("There are no user ids except the author for the reactions in post {} in guild {} for message {} in channel {}.", starboardPost.getId(), message.getChannelId(), message.getMessageId(), message.getChannelId());
        }
        userExceptAuthorIds.forEach(aLong -> {
            AUserInAServer user = userInServerManagementService.loadUserOptional(aLong).orElseThrow(() -> new UserInServerNotFoundException(aLong));
            starboardPostReactorManagementService.addReactor(starboardPost, user);
        });
        starboardPostCreatedListenerManager.sendStarboardPostCreatedEvent(userReactingId, starboardPost);
    }


    private CompletableFuture<StarboardPostModel> buildStarboardPostModel(CachedMessage message, Integer starCount)  {
        return userService.retrieveUserForId(message.getAuthor().getAuthorId()).thenApply(user -> {
            Optional<TextChannel> channel = channelService.getTextChannelFromServerOptional(message.getServerId(), message.getChannelId());
            Optional<Guild> guild = guildService.getGuildByIdOptional(message.getServerId());
            String starLevelEmote = getAppropriateEmote(message.getServerId(), starCount);
            return StarboardPostModel
                    .builder()
                    .message(message)
                    .author(user)
                    .sourceChannelId(message.getChannelId())
                    .channel(channel.orElse(null))
                    .starCount(starCount)
                    .guild(guild.orElse(null))
                    .starLevelEmote(starLevelEmote)
                    .build();
        });
    }

    @Override
    public CompletableFuture<Void> updateStarboardPost(StarboardPost post, CachedMessage message, List<AUserInAServer> userExceptAuthor)  {
        int starCount = userExceptAuthor.size();
        log.info("Updating starboard post {} in server {} with reactors {}.", post.getId(), post.getSourceChannel().getServer().getId(), starCount);
        return buildStarboardPostModel(message, starCount).thenCompose(starboardPostModel -> {
            MessageToSend messageToSend = templateService.renderEmbedTemplate(STARBOARD_POST_TEMPLATE, starboardPostModel, message.getServerId());
            List<CompletableFuture<Message>> futures = postTargetService.editOrCreatedInPostTarget(post.getStarboardMessageId(), messageToSend, StarboardPostTarget.STARBOARD, message.getServerId());
            Long starboardPostId = post.getId();
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenAccept(aVoid -> {
                Optional<StarboardPost> innerPost = starboardPostManagementService.findByStarboardPostId(starboardPostId);
                innerPost.ifPresent(starboardPost -> starboardPostManagementService.setStarboardPostMessageId(starboardPost, futures.get(0).join().getIdLong()));
            });
        });
    }

    @Override
    public void deleteStarboardMessagePost(StarboardPost message)  {
        AChannel starboardChannel = message.getStarboardChannel();
        log.info("Deleting starboard post {} in server {}", message.getId(), message.getSourceChannel().getServer().getId());
        messageService.deleteMessageInChannelInServer(starboardChannel.getServer().getId(), starboardChannel.getId(), message.getStarboardMessageId());
    }

    @Override
    public CompletableFuture<GuildStarStatsModel> retrieveStarStats(Long serverId)  {
        int count = 3;
        List<CompletableFuture<StarStatsUser>> topStarGiverFutures = starboardPostReactorManagementService.retrieveTopStarGiver(serverId, count);
        List<CompletableFuture<StarStatsUser>> topStarReceiverFutures = starboardPostReactorManagementService.retrieveTopStarReceiver(serverId, count);
        List<CompletableFuture> allFutures = new ArrayList<>();
        allFutures.addAll(topStarGiverFutures);
        allFutures.addAll(topStarReceiverFutures);
        return FutureUtils.toSingleFuture(allFutures).thenApply(aVoid -> {
            List<StarboardPost> starboardPosts = starboardPostManagementService.retrieveTopPosts(serverId, count);
            List<StarStatsPost> starStatsPosts = starboardPosts.stream().map(this::fromStarboardPost).sorted(Comparator.comparingLong(StarStatsPost::getStarCount).reversed()).collect(Collectors.toList());
            Long postCount = starboardPostManagementService.getPostCount(serverId);
            Integer reactionCount = starboardPostReactorManagementService.getStarCount(serverId);
            List<String> emotes = new ArrayList<>();
            for (int i = 1; i < count + 1; i++) {
                emotes.add(getStarboardRankingEmote(serverId, i));
            }
            List<StarStatsUser> topStarGivers = topStarGiverFutures.stream().map(CompletableFuture::join).collect(Collectors.toList());
            List<StarStatsUser> topStarReceiver = topStarReceiverFutures.stream().map(CompletableFuture::join).collect(Collectors.toList());
            return GuildStarStatsModel
                    .builder()
                    .badgeEmotes(emotes)
                    .starGiver(topStarGivers)
                    .starReceiver(topStarReceiver)
                    .topPosts(starStatsPosts)
                    .starredMessages(postCount)
                    .totalStars(reactionCount)
                    .build();
        });

    }

    @Override
    public MemberStarStatsModel retrieveStarStatsForMember(Member member) {
        int count = 3;
        Long receivedStars = starboardPostManagementService.retrieveReceivedStarsOfUserInServer(member.getGuild().getIdLong(), member.getIdLong());
        Long givenStars = starboardPostManagementService.retrieveGivenStarsOfUserInServer(member.getGuild().getIdLong(), member.getIdLong());
        List<StarboardPost> topPosts = starboardPostManagementService.retrieveTopPostsForUserInServer(member.getGuild().getIdLong(), member.getIdLong(), count);
        List<StarStatsPost> starStatsPosts = topPosts.stream().map(this::fromStarboardPost).sorted(Comparator.comparingLong(StarStatsPost::getStarCount).reversed()).collect(Collectors.toList());
        List<String> emotes = new ArrayList<>();
        for (int i = 1; i < count + 1; i++) {
            emotes.add(getStarboardRankingEmote(member.getGuild().getIdLong(), i));
        }
        return MemberStarStatsModel
                .builder()
                .member(member)
                .topPosts(starStatsPosts)
                .badgeEmotes(emotes)
                .receivedStars(receivedStars)
                .givenStars(givenStars)
                .build();
    }

    public StarStatsPost fromStarboardPost(StarboardPost starboardPost) {
        AChannel channel = starboardPost.getStarboardChannel();
        return StarStatsPost
                .builder()
                .serverId(starboardPost.getServer().getId())
                .channelId(channel.getId())
                .messageId(starboardPost.getPostMessageId())
                .starCount(starboardPostReactorManagementService.getReactorCountOfPost(starboardPost))
                .build();
    }

    @Override
    public void deleteStarboardPost(StarboardPost starboardPost, ServerUser userReacting) {
        deleteStarboardMessagePost(starboardPost);
        starboardPostManagementService.removePost(starboardPost);
        starboardPostDeletedListenerManager.sendStarboardPostDeletedEvent(starboardPost, userReacting);
    }

    private String getStarboardRankingEmote(Long serverId, Integer position) {
        return emoteService.getUsableEmoteOrDefault(serverId, buildBadgeName(position));
    }

    private String buildBadgeName(Integer position) {
        return StarboardFeatureConfig.STAR_BADGE_EMOTE_PREFIX + position;
    }

    private String getAppropriateEmote(Long serverId, Integer starCount) {
        int maxLevels = defaultConfigManagementService.getDefaultConfig(StarboardFeatureConfig.STAR_LEVELS_CONFIG_KEY).getLongValue().intValue();
        for(int i = maxLevels; i > 0; i--) {
            String key = StarboardFeatureConfig.STAR_LVL_CONFIG_PREFIX + i;
            SystemConfigProperty defaultStars = defaultConfigManagementService.getDefaultConfig(key);
            Long starMinimum = configService.getLongValue(key, serverId, defaultStars.getLongValue());
            if(starCount >= starMinimum) {
                return emoteService.getUsableEmoteOrDefault(serverId, StarboardFeatureConfig.STAR_EMOTE_PREFIX + i);
            }
        }
        return emoteService.getUsableEmoteOrDefault(serverId, StarboardFeatureConfig.STAR_EMOTE_PREFIX);
    }
}
