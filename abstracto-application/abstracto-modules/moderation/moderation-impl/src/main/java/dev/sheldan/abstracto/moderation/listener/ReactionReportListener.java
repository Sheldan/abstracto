package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncReactionAddedListener;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.listener.ReactionAddedModel;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.ReactionService;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.service.ReactionReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReactionReportListener implements AsyncReactionAddedListener {

    @Autowired
    private EmoteService emoteService;

    @Autowired
    private ReactionService reactionService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ReactionReportService reactionReportService;

    @Override
    public DefaultListenerResult execute(ReactionAddedModel model) {
        CachedMessage cachedMessage = model.getMessage();
        if(model.getUserReacting().getUserId().equals(cachedMessage.getAuthor().getAuthorId())) {
            return DefaultListenerResult.IGNORED;
        }

        if(model.getMemberReacting().getUser().isBot()) {
            return DefaultListenerResult.IGNORED;
        }

        Long serverId = model.getServerId();
        AEmote aEmote = emoteService.getEmoteOrDefaultEmote(ReactionReportService.REACTION_REPORT_EMOTE_KEY, serverId);
        if(emoteService.isReactionEmoteAEmote(model.getReaction().getReactionEmote(), aEmote)) {
            memberService.retrieveMemberInServer(model.getUserReacting())
                    .thenCompose(member -> reactionService.removeReactionFromMessage(model.getReaction(), cachedMessage, member.getUser()))
                    .thenAccept(unused -> log.info("Removed report reaction on message {} in server {} in channel {}.", cachedMessage.getMessageId(), serverId, cachedMessage.getChannelId()));
            log.info("User {} in server {} reacted to report a message {} from channel {}.",
                    model.getUserReacting().getUserId(), model.getServerId(), cachedMessage.getMessageId(), cachedMessage.getChannelId());
            reactionReportService.createReactionReport(cachedMessage, model.getUserReacting()).exceptionally(throwable -> {
                log.error("Failed to create reaction report in server {} on message {} in channel {}.", serverId, cachedMessage.getMessageId(), cachedMessage.getChannelId(), throwable);
                return null;
            });
            return DefaultListenerResult.PROCESSED;
        } else {
            return DefaultListenerResult.IGNORED;
        }
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.REPORT_REACTIONS;
    }
}
