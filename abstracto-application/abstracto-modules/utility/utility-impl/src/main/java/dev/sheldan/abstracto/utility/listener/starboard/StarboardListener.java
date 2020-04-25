package dev.sheldan.abstracto.utility.listener.starboard;

import dev.sheldan.abstracto.core.listener.ReactedAddedListener;
import dev.sheldan.abstracto.core.listener.ReactedRemovedListener;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReaction;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import dev.sheldan.abstracto.core.service.management.UserManagementService;
import dev.sheldan.abstracto.core.utils.EmoteUtils;
import dev.sheldan.abstracto.utility.config.UtilityFeatures;
import dev.sheldan.abstracto.utility.models.database.StarboardPost;
import dev.sheldan.abstracto.utility.service.StarboardService;
import dev.sheldan.abstracto.utility.service.management.StarboardPostManagementService;
import dev.sheldan.abstracto.utility.service.management.StarboardPostReactorManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.MessageReaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class StarboardListener implements ReactedAddedListener, ReactedRemovedListener {

    public static final String STAR_EMOTE = "star";

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
    private UserManagementService userManagementService;

    @Autowired
    private EmoteService emoteService;

    @Override
    @Transactional
    public void executeReactionAdded(CachedMessage message, MessageReaction addedReaction, AUserInAServer userAdding) {
        if(userAdding.getUserReference().getId().equals(message.getAuthorId())) {
            return;
        }
        Long guildId = message.getServerId();
        AEmote aEmote = emoteService.getEmoteOrFakeEmote(STAR_EMOTE, guildId);
        MessageReaction.ReactionEmote reactionEmote = addedReaction.getReactionEmote();
        Optional<Emote> emoteInGuild = botService.getEmote(guildId, aEmote);
        if(EmoteUtils.isReactionEmoteAEmote(reactionEmote, aEmote, emoteInGuild.orElse(null))) {
            log.trace("User {} in server {} reacted with star to put a message {} on starboard.", userAdding.getUserReference().getId(), userAdding.getServerReference().getId(), message.getMessageId());
            Optional<CachedReaction> reactionOptional = EmoteUtils.getReactionFromMessageByEmote(message, aEmote);
                updateStarboardPost(message, reactionOptional.orElse(null), userAdding, true);
        }
    }

    private void updateStarboardPost(CachedMessage message, CachedReaction reaction, AUserInAServer userReacting, boolean adding)  {
        Optional<StarboardPost> starboardPostOptional = starboardPostManagementService.findByMessageId(message.getMessageId());
        if(reaction != null) {
            List<AUser> userExceptAuthor = getUsersExcept(reaction.getUsers(), message.getAuthorId());
            Double starMinimum = getFromConfig("starLvl1", message.getServerId());
            if (userExceptAuthor.size() >= starMinimum) {
                log.info("Post reached starboard minimum. Message {} in channel {} in server {} will be starred/updated.",
                        message.getMessageId(), message.getChannelId(), message.getServerId());
                AUserInAServer author = userManagementService.loadUser(message.getServerId(), message.getAuthorId());
                if(starboardPostOptional.isPresent()) {
                    StarboardPost starboardPost = starboardPostOptional.get();
                    starboardPost.setIgnored(false);
                    starboardService.updateStarboardPost(starboardPost, message, userExceptAuthor);
                    if(adding) {
                        log.trace("Adding reactor {} from message {}", userReacting.getUserReference().getId(), message.getMessageId());
                        starboardPostReactorManagementService.addReactor(starboardPost, userReacting.getUserReference());
                    } else {
                        log.trace("Removing reactor {} from message {}", userReacting.getUserReference().getId(), message.getMessageId());
                        starboardPostReactorManagementService.removeReactor(starboardPost, userReacting.getUserReference());
                    }
                } else {
                    log.info("Creating starboard post for message {} in channel {} in server {}", message.getMessageId(), message.getChannelId(), message.getServerId());
                    starboardService.createStarboardPost(message, userExceptAuthor, userReacting, author);
                }
            } else {
                log.info("Removing starboard post for message {} in channel {} in server {}. It fell under the threshold {}", message.getMessageId(), message.getChannelId(), message.getServerId(), starMinimum);
                starboardPostOptional.ifPresent(this::completelyRemoveStarboardPost);
            }
        } else {
            log.info("Removing starboard post for message {} in channel {} in server {}", message.getMessageId(), message.getChannelId(), message.getServerId());
            starboardPostOptional.ifPresent(this::completelyRemoveStarboardPost);
        }
    }

    private void completelyRemoveStarboardPost(StarboardPost starboardPost)  {
        starboardPostReactorManagementService.removeReactors(starboardPost);
        starboardService.removeStarboardPost(starboardPost);
        starboardPostManagementService.removePost(starboardPost);
    }

    @Override
    @Transactional
    public void executeReactionRemoved(CachedMessage message, MessageReaction removedReaction, AUserInAServer userRemoving) {
        if(message.getAuthorId().equals(userRemoving.getUserReference().getId())) {
            return;
        }
        Long guildId = message.getServerId();
        AEmote aEmote = emoteService.getEmoteOrFakeEmote(STAR_EMOTE, guildId);
        MessageReaction.ReactionEmote reactionEmote = removedReaction.getReactionEmote();
        Optional<Emote> emoteInGuild = botService.getEmote(guildId, aEmote);
        if(EmoteUtils.isReactionEmoteAEmote(reactionEmote, aEmote, emoteInGuild.orElse(null))) {
            log.trace("User {} in server {} removed star reaction from message {} on starboard.",
                    userRemoving.getUserReference().getId(), userRemoving.getServerReference().getId(), message.getMessageId());
            Optional<CachedReaction> reactionOptional = EmoteUtils.getReactionFromMessageByEmote(message, aEmote);
                updateStarboardPost(message, reactionOptional.orElse(null), userRemoving, false);
        }
    }

    private Double getFromConfig(String key, Long guildId) {
        return configManagementService.loadConfig(guildId, key).getDoubleValue();
    }

    private List<AUser> getUsersExcept(List<AUser> users, Long userId) {
        return users.stream().filter(user -> !user.getId().equals(userId)).collect(Collectors.toList());
    }

    @Override
    public String getFeature() {
        return UtilityFeatures.STARBOARD;
    }
}
