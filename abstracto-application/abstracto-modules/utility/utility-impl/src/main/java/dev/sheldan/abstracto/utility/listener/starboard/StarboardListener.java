package dev.sheldan.abstracto.utility.listener.starboard;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.listener.ReactedAddedListener;
import dev.sheldan.abstracto.core.listener.ReactedRemovedListener;
import dev.sheldan.abstracto.core.listener.ReactionClearedListener;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReaction;
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
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class StarboardListener implements ReactedAddedListener, ReactedRemovedListener, ReactionClearedListener {

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

    @Override
    @Transactional
    public void executeReactionAdded(CachedMessage message, GuildMessageReactionAddEvent addedReaction, AUserInAServer userAdding) {
        if(userAdding.getUserReference().getId().equals(message.getAuthorId())) {
            return;
        }
        Long guildId = message.getServerId();
        AEmote aEmote = emoteService.getEmoteOrDefaultEmote(STAR_EMOTE, guildId);
        MessageReaction.ReactionEmote reactionEmote = addedReaction.getReactionEmote();
        if(emoteService.isReactionEmoteAEmote(reactionEmote, aEmote)) {
            log.trace("User {} in server {} reacted with star to put a message {} on starboard.", userAdding.getUserReference().getId(), userAdding.getServerReference().getId(), message.getMessageId());
            Optional<CachedReaction> reactionOptional = emoteService.getReactionFromMessageByEmote(message, aEmote);
                handleStarboardPostChange(message, reactionOptional.orElse(null), userAdding, true);
        }
    }

    private void handleStarboardPostChange(CachedMessage message, CachedReaction reaction, AUserInAServer userReacting, boolean adding)  {
        Optional<StarboardPost> starboardPostOptional = starboardPostManagementService.findByMessageId(message.getMessageId());
        if(reaction != null) {
            AUserInAServer author = userInServerManagementService.loadUser(message.getServerId(), message.getAuthorId());
            List<AUserInAServer> userExceptAuthor = getUsersExcept(reaction.getUserInServersIds(), author);
            Long starMinimum = getFromConfig(FIRST_LEVEL_THRESHOLD_KEY, message.getServerId());
            if (userExceptAuthor.size() >= starMinimum) {
                log.info("Post reached starboard minimum. Message {} in channel {} in server {} will be starred/updated.",
                        message.getMessageId(), message.getChannelId(), message.getServerId());
                if(starboardPostOptional.isPresent()) {
                    updateStarboardPost(message, userReacting, adding, starboardPostOptional.get(), userExceptAuthor);
                } else {
                    log.info("Creating starboard post for message {} in channel {} in server {}", message.getMessageId(), message.getChannelId(), message.getServerId());
                    starboardService.createStarboardPost(message, userExceptAuthor, userReacting, author);
                }
            } else {
                if(starboardPostOptional.isPresent()) {
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
    public void executeReactionRemoved(CachedMessage message, GuildMessageReactionRemoveEvent removedReaction, AUserInAServer userRemoving) {
        if(message.getAuthorId().equals(userRemoving.getUserReference().getId())) {
            return;
        }
        Long guildId = message.getServerId();
        AEmote aEmote = emoteService.getEmoteOrDefaultEmote(STAR_EMOTE, guildId);
        MessageReaction.ReactionEmote reactionEmote = removedReaction.getReactionEmote();
        if(emoteService.isReactionEmoteAEmote(reactionEmote, aEmote)) {
            log.trace("User {} in server {} removed star reaction from message {} on starboard.",
                    userRemoving.getUserReference().getId(), userRemoving.getServerReference().getId(), message.getMessageId());
            Optional<CachedReaction> reactionOptional = emoteService.getReactionFromMessageByEmote(message, aEmote);
            handleStarboardPostChange(message, reactionOptional.orElse(null), userRemoving, false);
        }
    }

    private Long getFromConfig(String key, Long guildId) {
        return configManagementService.loadConfig(guildId, key).getLongValue();
    }

    private List<AUserInAServer> getUsersExcept(List<Long> users, AUserInAServer author) {
        return users.stream().filter(user -> !user.equals(author.getUserInServerId())).map(aLong -> {
            Optional<AUserInAServer> aUserInAServer = userInServerManagementService.loadUserConditional(aLong);
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
            starboardPostReactorManagementService.removeReactors(starboardPost);
            completelyRemoveStarboardPost(starboardPost);
        });
    }
}
