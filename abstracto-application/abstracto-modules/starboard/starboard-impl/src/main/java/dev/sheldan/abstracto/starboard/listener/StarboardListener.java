package dev.sheldan.abstracto.starboard.listener;

import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReactions;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.starboard.model.database.StarboardPost;
import dev.sheldan.abstracto.starboard.service.StarboardService;
import dev.sheldan.abstracto.starboard.service.management.StarboardPostManagementService;
import dev.sheldan.abstracto.starboard.service.management.StarboardPostReactorManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public abstract class StarboardListener {

    public static final String FIRST_LEVEL_THRESHOLD_KEY = "starLvl1";

    @Autowired
    protected ConfigManagementService configManagementService;

    @Autowired
    protected StarboardService starboardService;

    @Autowired
    protected StarboardPostManagementService starboardPostManagementService;

    @Autowired
    protected StarboardPostReactorManagementService starboardPostReactorManagementService;

    @Autowired
    protected UserInServerManagementService userInServerManagementService;

    @Autowired
    protected EmoteService emoteService;

    @Autowired
    protected MetricService metricService;

    public static final String STARBOARD_STARS = "starboard.stars";
    public static final String STARBOARD_POSTS = "starboard.posts";
    public static final String STAR_ACTION = "action";

    protected static final CounterMetric STARBOARD_STARS_THRESHOLD_REACHED = CounterMetric
            .builder()
            .name(STARBOARD_POSTS)
            .tagList(Arrays.asList(MetricTag.getTag(STAR_ACTION, "threshold.reached")))
            .build();

    protected static final CounterMetric STARBOARD_STARS_THRESHOLD_FELL = CounterMetric
            .builder()
            .name(STARBOARD_POSTS)
            .tagList(Arrays.asList(MetricTag.getTag(STAR_ACTION, "threshold.below")))
            .build();

    protected void handleStarboardPostChange(CachedMessage message, CachedReactions reaction, ServerUser userReacting, boolean adding)  {
        Optional<StarboardPost> starboardPostOptional = starboardPostManagementService.findByMessageId(message.getMessageId());
        if(reaction != null) {
            AUserInAServer author = userInServerManagementService.loadOrCreateUser(message.getServerId(), message.getAuthor().getAuthorId());
            List<AUserInAServer> userExceptAuthor = getUsersExcept(reaction.getUsers(), author);
            Long starMinimum = getFromConfig(FIRST_LEVEL_THRESHOLD_KEY, message.getServerId());
            AUserInAServer userAddingReaction = userInServerManagementService.loadOrCreateUser(userReacting);
            if (userExceptAuthor.size() >= starMinimum) {
                log.info("Post reached starboard minimum. Message {} in channel {} in server {} will be starred/updated.",
                        message.getMessageId(), message.getChannelId(), message.getServerId());
                if(starboardPostOptional.isPresent()) {
                    updateStarboardPost(message, userAddingReaction, adding, starboardPostOptional.get(), userExceptAuthor);
                } else {
                    metricService.incrementCounter(STARBOARD_STARS_THRESHOLD_REACHED);
                    log.info("Creating starboard post for message {} in channel {} in server {}", message.getMessageId(), message.getChannelId(), message.getServerId());
                    starboardService.createStarboardPost(message, userExceptAuthor, userAddingReaction, author).exceptionally(throwable -> {
                        log.error("Failed to persist starboard post for message {}.", message.getMessageId(), throwable);
                        return null;
                    });
                }
            } else {
                if(starboardPostOptional.isPresent()) {
                    metricService.incrementCounter(STARBOARD_STARS_THRESHOLD_FELL);
                    log.info("Removing starboard post for message {} in channel {} in server {}. It fell under the threshold {}", message.getMessageId(), message.getChannelId(), message.getServerId(), starMinimum);
                    starboardPostOptional.ifPresent(starboardPost -> completelyRemoveStarboardPost(starboardPost, userReacting));
                }
            }
        } else {
            if(starboardPostOptional.isPresent()) {
                log.info("Removing starboard post for message {} in channel {} in server {}", message.getMessageId(), message.getChannelId(), message.getServerId());
                starboardPostOptional.ifPresent(starboardPost -> completelyRemoveStarboardPost(starboardPost, userReacting));
            }
        }
    }

    protected void updateStarboardPost(CachedMessage message, AUserInAServer userReacting, boolean adding, StarboardPost starboardPost, List<AUserInAServer> userExceptAuthor) {
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

    protected void completelyRemoveStarboardPost(StarboardPost starboardPost, ServerUser userReacting)  {
        starboardService.deleteStarboardPost(starboardPost, userReacting);
    }

    protected Long getFromConfig(String key, Long guildId) {
        return configManagementService.loadConfig(guildId, key).getLongValue();
    }

    protected List<AUserInAServer> getUsersExcept(List<ServerUser> users, AUserInAServer author) {
        return users.stream().filter(user -> !(user.getServerId().equals(author.getServerReference().getId()) && user.getUserId().equals(author.getUserReference().getId()))).map(serverUser -> {
            Optional<AUserInAServer> aUserInAServer = userInServerManagementService.loadUserOptional(serverUser.getServerId(), serverUser.getUserId());
            return aUserInAServer.orElse(null);
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(STARBOARD_STARS_THRESHOLD_REACHED, "Starboard posts reaching threshold");
        metricService.registerCounter(STARBOARD_STARS_THRESHOLD_FELL, "Starboard posts falling below threshold");
    }
}