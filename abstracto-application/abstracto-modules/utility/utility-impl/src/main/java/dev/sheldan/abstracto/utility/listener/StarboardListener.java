package dev.sheldan.abstracto.utility.listener;

import dev.sheldan.abstracto.core.listener.ReactedAddedListener;
import dev.sheldan.abstracto.core.listener.ReactedRemovedListener;
import dev.sheldan.abstracto.core.management.EmoteManagementService;
import dev.sheldan.abstracto.core.management.UserManagementService;
import dev.sheldan.abstracto.core.models.CachedMessage;
import dev.sheldan.abstracto.core.models.CachedReaction;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.Bot;
import dev.sheldan.abstracto.core.management.ConfigManagementService;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.core.utils.EmoteUtils;
import dev.sheldan.abstracto.utility.models.StarboardPost;
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

    public static final String STAR_EMOTE = "STAR";

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

    @Override
    @Transactional
    public void executeReactionAdded(CachedMessage message, MessageReaction addedReaction, AUserInAServer userAdding) {
        if(userAdding.getUserReference().getId().equals(message.getAuthorId())) {
            return;
        }
        Long guildId = message.getServerId();
        Optional<AEmote> aEmote = emoteManagementService.loadEmoteByName(STAR_EMOTE, guildId);
        if(aEmote.isPresent()) {
            AEmote emote = aEmote.get();
            MessageReaction.ReactionEmote reactionEmote = addedReaction.getReactionEmote();
            Emote emoteInGuild = bot.getEmote(guildId, emote);
            if(EmoteUtils.isReactionEmoteAEmote(reactionEmote, emote, Optional.ofNullable(emoteInGuild))) {
                Optional<CachedReaction> reactionOptional = EmoteUtils.getReactionFromMessageByEmote(message, emote);
                updateStarboardPost(message, reactionOptional.orElse(null), userAdding, true);
            }
        } else {
            log.warn("Emote {} is not defined for guild {}. Starboard not functional.", STAR_EMOTE, guildId);
        }
    }

    private void updateStarboardPost(CachedMessage message, CachedReaction reaction, AUserInAServer userReacting, boolean adding) {
        Optional<StarboardPost> starboardPostOptional = starboardPostManagementService.findByMessageId(message.getMessageId());
        if(reaction != null) {
            List<AUser> userExceptAuthor = getUsersExcept(reaction.getUsers(), message.getAuthorId());
            Double starMinimum = getFromConfig("starLvl1", message.getServerId());
            if (userExceptAuthor.size() >= starMinimum) {
                AUserInAServer author = userManagementService.loadUser(message.getServerId(), message.getAuthorId());
                if(starboardPostOptional.isPresent()) {
                    StarboardPost starboardPost = starboardPostOptional.get();
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
                starboardPostOptional.ifPresent(starboardPost ->  {
                    starboardService.removeStarboardPost(starboardPost);
                    starboardPostReactorManagementService.removeReactors(starboardPost);
                });
            }
        } else {
            starboardPostOptional.ifPresent(starboardPost -> {
                starboardService.removeStarboardPost(starboardPost);
                starboardPostReactorManagementService.removeReactors(starboardPost);
            });
        }
    }

    @Override
    @Transactional
    public void executeReactionRemoved(CachedMessage message, MessageReaction removedReaction, AUserInAServer userRemoving) {
        if(message.getAuthorId().equals(userRemoving.getUserReference().getId())) {
            return;
        }
        Long guildId = message.getServerId();
        Optional<AEmote> aEmote = emoteManagementService.loadEmoteByName(STAR_EMOTE, guildId);
        if(aEmote.isPresent()) {
            AEmote emote = aEmote.get();
            MessageReaction.ReactionEmote reactionEmote = removedReaction.getReactionEmote();
            Emote emoteInGuild = bot.getEmote(guildId, emote);
            if(EmoteUtils.isReactionEmoteAEmote(reactionEmote, emote, Optional.ofNullable(emoteInGuild))) {
                Optional<CachedReaction> reactionOptional = EmoteUtils.getReactionFromMessageByEmote(message, emote);
                updateStarboardPost(message, reactionOptional.orElse(null), userRemoving, false);
            }
        } else {
            log.warn("Emote {} is not defined for guild {}. Starboard not functional.", STAR_EMOTE, guildId);
        }
    }

    private Double getFromConfig(String key, Long guildId) {
        return configManagementService.loadConfig(guildId, key).getDoubleValue();
    }

    private List<AUser> getUsersExcept(List<AUser> users, Long userId) {
        return users.stream().filter(user -> !user.getId().equals(userId)).collect(Collectors.toList());
    }
}
