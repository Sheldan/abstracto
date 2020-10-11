package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.exception.UserInServerNotFoundException;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import dev.sheldan.abstracto.core.service.management.PostTargetManagement;
import dev.sheldan.abstracto.core.models.AServerAChannelMessage;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.templating.service.TemplateService;
import dev.sheldan.abstracto.utility.config.posttargets.StarboardPostTarget;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;


@Component
@Slf4j
public class StarboardServiceBean implements StarboardService {

    public static final String STARBOARD_POST_TEMPLATE = "starboard_post";
    public static final String STAR_LVL_CONFIG_PREFIX = "starLvl";
    public static final String STAR_LEVELS_CONFIG_KEY = "starLvls";

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
    private StarboardServiceBean self;

    @Override
    public CompletableFuture<Void> createStarboardPost(CachedMessage message, List<AUserInAServer> userExceptAuthor, AUserInAServer userReacting, AUserInAServer starredUser)  {
        Long starredUserId = starredUser.getUserInServerId();
        List<Long> userExceptAuthorIds = userExceptAuthor.stream().map(AUserInAServer::getUserInServerId).collect(Collectors.toList());
        return buildStarboardPostModel(message, userExceptAuthor.size()).thenCompose(starboardPostModel ->
            self.sendStarboardPostAndStore(message, starredUserId, userExceptAuthorIds, starboardPostModel)
        );

    }

    @Transactional
    public CompletionStage<Void> sendStarboardPostAndStore(CachedMessage message, Long starredUserId, List<Long> userExceptAuthorIds, StarboardPostModel starboardPostModel) {
        MessageToSend messageToSend = templateService.renderEmbedTemplate(STARBOARD_POST_TEMPLATE, starboardPostModel);
        PostTarget starboard = postTargetManagement.getPostTarget(StarboardPostTarget.STARBOARD.getKey(), message.getServerId());
        List<CompletableFuture<Message>> completableFutures = postTargetService.sendEmbedInPostTarget(messageToSend, StarboardPostTarget.STARBOARD, message.getServerId());
        Long starboardChannelId = starboard.getChannelReference().getId();
        return CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).thenAccept(aVoid ->
                self.persistPost(message, userExceptAuthorIds, completableFutures, starboardChannelId, starredUserId)
        );
    }

    @Transactional
    public void persistPost(CachedMessage message, List<Long> userExceptAuthorIds, List<CompletableFuture<Message>> completableFutures, Long starboardChannelId, Long starredUserId) {
        AUserInAServer innerStarredUser = userInServerManagementService.loadUserConditional(starredUserId).orElseThrow(() -> new UserInServerNotFoundException(starredUserId));
        AChannel starboardChannel = channelManagementService.loadChannel(starboardChannelId);
        Message message1 = completableFutures.get(0).join();
        AServerAChannelMessage aServerAChannelMessage = AServerAChannelMessage
                .builder()
                .messageId(message1.getIdLong())
                .channel(starboardChannel)
                .server(starboardChannel.getServer())
                .build();
        StarboardPost starboardPost = starboardPostManagementService.createStarboardPost(message, innerStarredUser, aServerAChannelMessage);
        log.info("Persisting starboard post in channel {} with message {} with {} reactors.", message1.getId(),starboardChannelId, userExceptAuthorIds.size());
        if(userExceptAuthorIds.isEmpty()) {
            log.warn("There are no user ids except the author for the reactions in post {} in guild {} for message {} in channel {}.", starboardPost.getId(), message.getChannelId(), message.getMessageId(), message.getChannelId());
        }
        userExceptAuthorIds.forEach(aLong -> {
            AUserInAServer user = userInServerManagementService.loadUserConditional(aLong).orElseThrow(() -> new UserInServerNotFoundException(aLong));
            starboardPostReactorManagementService.addReactor(starboardPost, user);
        });
    }

    private CompletableFuture<StarboardPostModel> buildStarboardPostModel(CachedMessage message, Integer starCount)  {
        return botService.getMemberInServerAsync(message.getServerId(), message.getAuthorId()).thenApply(member -> {
            Optional<TextChannel> channel = botService.getTextChannelFromServerOptional(message.getServerId(), message.getChannelId());
            Optional<Guild> guild = botService.getGuildByIdOptional(message.getServerId());
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
        });
    }

    @Override
    public CompletableFuture<Void> updateStarboardPost(StarboardPost post, CachedMessage message, List<AUserInAServer> userExceptAuthor)  {
        int starCount = userExceptAuthor.size();
        log.info("Updating starboard post {} in server {} with reactors {}.", post.getId(), post.getSourceChanel().getServer().getId(), starCount);
        return buildStarboardPostModel(message, starCount).thenCompose(starboardPostModel -> {
            MessageToSend messageToSend = templateService.renderEmbedTemplate(STARBOARD_POST_TEMPLATE, starboardPostModel);
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
        log.info("Deleting starboard post {} in server {}", message.getId(), message.getSourceChanel().getServer().getId());
        botService.deleteMessage(starboardChannel.getServer().getId(), starboardChannel.getId(), message.getStarboardMessageId());
    }

    @Override
    public CompletableFuture<StarStatsModel> retrieveStarStats(Long serverId)  {
        int count = 3;
        List<CompletableFuture<StarStatsUser>> topStarGiverFutures = starboardPostReactorManagementService.retrieveTopStarGiver(serverId, count);
        List<CompletableFuture<StarStatsUser>> topStarReceiverFutures = starboardPostReactorManagementService.retrieveTopStarReceiver(serverId, count);
        List<CompletableFuture> allFutures = new ArrayList<>();
        allFutures.addAll(topStarGiverFutures);
        allFutures.addAll(topStarReceiverFutures);
        return FutureUtils.toSingleFuture(allFutures).thenApply(aVoid -> {
            List<StarboardPost> starboardPosts = starboardPostManagementService.retrieveTopPosts(serverId, count);
            List<StarStatsPost> starStatsPosts = starboardPosts.stream().map(this::fromStarboardPost).collect(Collectors.toList());
            Integer postCount = starboardPostManagementService.getPostCount(serverId);
            Integer reactionCount = starboardPostReactorManagementService.getStarCount(serverId);
            List<String> emotes = new ArrayList<>();
            for (int i = 1; i < count + 1; i++) {
                emotes.add(getStarboardRankingEmote(serverId, i));
            }
            List<StarStatsUser> topStarGivers = topStarGiverFutures.stream().map(CompletableFuture::join).collect(Collectors.toList());
            List<StarStatsUser> topStarReceiver = topStarReceiverFutures.stream().map(CompletableFuture::join).collect(Collectors.toList());
            return StarStatsModel
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

    public StarStatsPost fromStarboardPost(StarboardPost starboardPost) {
        AChannel channel = starboardPost.getStarboardChannel();
        return StarStatsPost
                .builder()
                .serverId(channel.getServer().getId())
                .channelId(channel.getId())
                .messageId(starboardPost.getPostMessageId())
                .starCount(starboardPost.getReactions().size())
                .build();
    }

    private String getStarboardRankingEmote(Long serverId, Integer position) {
        return emoteService.getUsableEmoteOrDefault(serverId, buildBadgeName(position));
    }

    private String buildBadgeName(Integer position) {
        return "starboardBadge" + position;
    }

    private String getAppropriateEmote(Long serverId, Integer starCount) {
        int maxLevels = defaultConfigManagementService.getDefaultConfig(StarboardServiceBean.STAR_LEVELS_CONFIG_KEY).getLongValue().intValue();
        for(int i = maxLevels; i > 0; i--) {
            Long starMinimum = configService.getLongValue(STAR_LVL_CONFIG_PREFIX + i, serverId);
            if(starCount >= starMinimum) {
                return emoteService.getUsableEmoteOrDefault(serverId, "star" + i);
            }
        }
        return emoteService.getUsableEmoteOrDefault(serverId, "star0");
    }
}
