package dev.sheldan.abstracto.utility.listener.starboard;

import dev.sheldan.abstracto.core.listener.ReactedAddedListener;
import dev.sheldan.abstracto.core.listener.ReactedRemovedListener;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReaction;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.Bot;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import dev.sheldan.abstracto.core.service.management.EmoteManagementService;
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
    private EmoteManagementService emoteManagementService;

    @Autowired
    private Bot bot;

    @Autowired
    private MessageCache messageCache;

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
        Optional<Emote> emoteInGuild = bot.getEmote(guildId, aEmote);
        if(EmoteUtils.isReactionEmoteAEmote(reactionEmote, aEmote, emoteInGuild.orElse(null))) {
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
                AUserInAServer author = userManagementService.loadUser(message.getServerId(), message.getAuthorId());
                if(starboardPostOptional.isPresent()) {
                    StarboardPost starboardPost = starboardPostOptional.get();
                    starboardPost.setIgnored(false);
                    starboardService.updateStarboardPost(starboardPost, message, userExceptAuthor);
                    if(adding) {
                        starboardPostReactorManagementService.addReactor(starboardPost, userReacting.getUserReference());
                    } else {
                        starboardPostReactorManagementService.removeReactor(starboardPost, userReacting.getUserReference());
                    }
                } else {
                    starboardService.createStarboardPost(message, userExceptAuthor, userReacting, author);
                }
            } else {
                if(starboardPostOptional.isPresent()) {
                    this.completelyRemoveStarboardPost(starboardPostOptional.get());
                }
            }
        } else {
            if(starboardPostOptional.isPresent()) {
                this.completelyRemoveStarboardPost(starboardPostOptional.get());
            }
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
        Optional<Emote> emoteInGuild = bot.getEmote(guildId, aEmote);
        if(EmoteUtils.isReactionEmoteAEmote(reactionEmote, aEmote, emoteInGuild.orElse(null))) {
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
