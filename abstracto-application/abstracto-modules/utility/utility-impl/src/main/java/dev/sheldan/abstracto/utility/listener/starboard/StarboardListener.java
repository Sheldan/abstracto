package dev.sheldan.abstracto.utility.listener.starboard;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncReactionAddedListener;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncReactionClearedListener;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncReactionRemovedListener;
import dev.sheldan.abstracto.core.metrics.service.CounterMetric;
import dev.sheldan.abstracto.core.metrics.service.MetricService;
import dev.sheldan.abstracto.core.metrics.service.MetricTag;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReactions;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.utility.config.features.UtilityFeature;
import dev.sheldan.abstracto.utility.models.database.StarboardPost;
import dev.sheldan.abstracto.utility.service.StarboardService;
import dev.sheldan.abstracto.utility.service.management.StarboardPostManagementService;
import dev.sheldan.abstracto.utility.service.management.StarboardPostReactorManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class StarboardListener implements AsyncReactionAddedListener, AsyncReactionRemovedListener, AsyncReactionClearedListener {

    public static final String STAR_EMOTE = "star";
    public static final String FIRST_LEVEL_THRESHOLD_KEY = "starLvl1";

    @Autowired
    private BotService botService;

    @Autowired
    private ConfigManagementService configManagementService;

    @Autowired
    private StarboardService starboardService;

    @Autowired
    private StarboardPostManagementService starboardPostManagementService;

    @Autowired
    private StarboardPostReactorManagementService starboardPostReactorManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private EmoteService emoteService;

    @Autowired
    private MetricService metricService;

    public static final String STARBOARD_STARS = "starboard.stars";
    public static final String STARBOARD_POSTS = "starboard.posts";
    public static final String STAR_ACTION = "action";
    private static final CounterMetric STARBOARD_STARS_ADDED = CounterMetric
            .builder()
            .name(STARBOARD_STARS)
            .tagList(Arrays.asList(MetricTag.getTag(STAR_ACTION, "added")))
            .build();

    private static final CounterMetric STARBOARD_STARS_REMOVED = CounterMetric
            .builder()
            .name(STARBOARD_STARS)
            .tagList(Arrays.asList(MetricTag.getTag(STAR_ACTION, "removed")))
            .build();

    private static final CounterMetric STARBOARD_STARS_THRESHOLD_REACHED = CounterMetric
            .builder()
            .name(STARBOARD_POSTS)
            .tagList(Arrays.asList(MetricTag.getTag(STAR_ACTION, "threshold.reached")))
            .build();

    private static final CounterMetric STARBOARD_STARS_THRESHOLD_FELL = CounterMetric
            .builder()
            .name(STARBOARD_POSTS)
            .tagList(Arrays.asList(MetricTag.getTag(STAR_ACTION, "threshold.below")))
            .build();

    @Override
    @Transactional
    public void executeReactionAdded(CachedMessage message, CachedReactions cachedReaction, ServerUser serverUser) {
        if(serverUser.getUserId().equals(message.getAuthor().getAuthorId())) {
            return;
        }
        Long guildId = message.getServerId();
        AEmote aEmote = emoteService.getEmoteOrDefaultEmote(STAR_EMOTE, guildId);
        if(emoteService.compareCachedEmoteWithAEmote(cachedReaction.getEmote(), aEmote)) {
            metricService.incrementCounter(STARBOARD_STARS_ADDED);
            log.info("User {} in server {} reacted with star to put a message {} from channel {} on starboard.", serverUser.getUserId(), message.getServerId(), message.getMessageId(), message.getChannelId());
            Optional<CachedReactions> reactionOptional = emoteService.getReactionFromMessageByEmote(message, aEmote);
                handleStarboardPostChange(message, reactionOptional.orElse(null), serverUser, true);
        }
    }

    private void handleStarboardPostChange(CachedMessage message, CachedReactions reaction, ServerUser serverUser, boolean adding)  {
        Optional<StarboardPost> starboardPostOptional = starboardPostManagementService.findByMessageId(message.getMessageId());
        if(reaction != null) {
            AUserInAServer author = userInServerManagementService.loadOrCreateUser(message.getServerId(), message.getAuthor().getAuthorId());
            List<AUserInAServer> userExceptAuthor = getUsersExcept(reaction.getUsers(), author);
            Long starMinimum = getFromConfig(FIRST_LEVEL_THRESHOLD_KEY, message.getServerId());
            AUserInAServer userAddingReaction = userInServerManagementService.loadOrCreateUser(serverUser);
            if (userExceptAuthor.size() >= starMinimum) {
                log.info("Post reached starboard minimum. Message {} in channel {} in server {} will be starred/updated.",
                        message.getMessageId(), message.getChannelId(), message.getServerId());
                if(starboardPostOptional.isPresent()) {
                    updateStarboardPost(message, userAddingReaction, adding, starboardPostOptional.get(), userExceptAuthor);
                } else {
                    metricService.incrementCounter(STARBOARD_STARS_THRESHOLD_REACHED);
                    log.info("Creating starboard post for message {} in channel {} in server {}", message.getMessageId(), message.getChannelId(), message.getServerId());
                    starboardService.createStarboardPost(message, userExceptAuthor, userAddingReaction, author);
                }
            } else {
                if(starboardPostOptional.isPresent()) {
                    metricService.incrementCounter(STARBOARD_STARS_THRESHOLD_FELL);
                    log.info("Removing starboard post for message {} in channel {} in server {}. It fell under the threshold {}", message.getMessageId(), message.getChannelId(), message.getServerId(), starMinimum);
                    starboardPostOptional.ifPresent(this::completelyRemoveStarboardPost);
                }
            }
        } else {
            if(starboardPostOptional.isPresent()) {
                log.info("Removing starboard post for message {} in channel {} in server {}", message.getMessageId(), message.getChannelId(), message.getServerId());
                starboardPostOptional.ifPresent(this::completelyRemoveStarboardPost);
            }
        }
    }

    private void updateStarboardPost(CachedMessage message, AUserInAServer userReacting, boolean adding, StarboardPost starboardPost, List<AUserInAServer> userExceptAuthor) {
        starboardPost.setIgnored(false);
        // TODO handle futures correctly
        starboardService.updateStarboardPost(starboardPost, message, userExceptAuthor);
        if(adding) {
            log.trace("Adding reactor {} from message {}", userReacting.getUserReference().getId(), message.getMessageId());
            starboardPostReactorManagementService.addReactor(starboardPost, userReacting);
        } else {
            log.trace("Removing reactor {} from message {}", userReacting.getUserReference().getId(), message.getMessageId());
            starboardPostReactorManagementService.removeReactor(starboardPost, userReacting);
        }
    }

    private void completelyRemoveStarboardPost(StarboardPost starboardPost)  {
        starboardService.deleteStarboardMessagePost(starboardPost);
        starboardPostManagementService.removePost(starboardPost);
    }

    @Override
    @Transactional
    public void executeReactionRemoved(CachedMessage message, CachedReactions removedReaction, ServerUser userRemoving) {
        if(message.getAuthor().getAuthorId().equals(userRemoving.getUserId())) {
            return;
        }
        Long guildId = message.getServerId();
        AEmote aEmote = emoteService.getEmoteOrDefaultEmote(STAR_EMOTE, guildId);
        if(emoteService.compareCachedEmoteWithAEmote(removedReaction.getEmote(), aEmote)) {
            metricService.incrementCounter(STARBOARD_STARS_REMOVED);
            log.info("User {} in server {} removed star reaction from message {} on starboard.",
                    userRemoving.getUserId(), message.getServerId(), message.getMessageId());
            Optional<CachedReactions> reactionOptional = emoteService.getReactionFromMessageByEmote(message, aEmote);
            handleStarboardPostChange(message, reactionOptional.orElse(null), userRemoving, false);
        }
    }

    private Long getFromConfig(String key, Long guildId) {
        return configManagementService.loadConfig(guildId, key).getLongValue();
    }

    private List<AUserInAServer> getUsersExcept(List<ServerUser> users, AUserInAServer author) {
        return users.stream().filter(user -> !(user.getServerId().equals(author.getServerReference().getId()) && user.getUserId().equals(author.getUserReference().getId()))).map(serverUser -> {
            Optional<AUserInAServer> aUserInAServer = userInServerManagementService.loadUserOptional(serverUser.getServerId(), serverUser.getUserId());
            return aUserInAServer.orElse(null);
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public FeatureEnum getFeature() {
        return UtilityFeature.STARBOARD;
    }

    @Override
    public void executeReactionCleared(CachedMessage message) {
        Optional<StarboardPost> starboardPostOptional = starboardPostManagementService.findByMessageId(message.getMessageId());

        starboardPostOptional.ifPresent(starboardPost -> {
            log.info("Reactions on message {} in channel {} in server {} were cleared. Completely deleting the starboard post {}.",
                    message.getMessageId(), message.getChannelId(), message.getServerId(), starboardPost.getId());
            starboardPostReactorManagementService.removeReactors(starboardPost);
            completelyRemoveStarboardPost(starboardPost);
        });
    }

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(STARBOARD_STARS_ADDED, "Star reaction added");
        metricService.registerCounter(STARBOARD_STARS_REMOVED, "Star reaction removed");
        metricService.registerCounter(STARBOARD_STARS_THRESHOLD_REACHED, "Starboard posts reaching threshold");
        metricService.registerCounter(STARBOARD_STARS_THRESHOLD_FELL, "Starboard posts falling below threshold");
    }

}
