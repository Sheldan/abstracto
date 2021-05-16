package dev.sheldan.abstracto.profanityfilter.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncReactionAddedListener;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReactions;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.listener.ReactionAddedModel;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.profanityfilter.config.ProfanityFilterFeatureDefinition;
import dev.sheldan.abstracto.profanityfilter.model.database.ProfanityUse;
import dev.sheldan.abstracto.profanityfilter.service.ProfanityFilterService;
import dev.sheldan.abstracto.profanityfilter.service.management.ProfanityUseManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static dev.sheldan.abstracto.profanityfilter.service.ProfanityFilterService.PROFANITY_VOTES_CONFIG_KEY;

@Component
public class ProfanityReportVoteListener implements AsyncReactionAddedListener {

    @Autowired
    private EmoteService emoteService;

    @Autowired
    private ProfanityUseManagementService profanityUseManagementService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private ProfanityFilterService profanityFilterService;

    @Override
    public DefaultListenerResult execute(ReactionAddedModel model) {
        Optional<ProfanityUse> profanityUseOptional = profanityUseManagementService.getProfanityUseViaReportMessageId(model.getMessage().getMessageId());
        if(profanityUseOptional.isPresent()) {
            ProfanityUse use = profanityUseOptional.get();
            if(use.getVerified()) {
                return DefaultListenerResult.PROCESSED;
            }
            AEmote addedEmote = emoteService.buildAEmoteFromReaction(model.getReaction().getReactionEmote());
            AEmote agreeEmote = emoteService.getEmoteOrDefaultEmote(ProfanityFilterService.REPORT_AGREE_EMOTE, model.getServerId());
            boolean isAgreement = emoteService.compareAEmote(addedEmote, agreeEmote);
            boolean reactionWasVote;
            AEmote disApproveEmote = emoteService.getEmoteOrDefaultEmote(ProfanityFilterService.REPORT_DISAGREE_EMOTE, model.getServerId());
            if(!isAgreement) {
                reactionWasVote =  emoteService.compareAEmote(addedEmote, disApproveEmote);
            } else {
                reactionWasVote = true;
            }
            if(reactionWasVote) {
                ProfanityFilterService.VoteResult voteResult = getVoteResultOnMessage(model.getMessage(), agreeEmote, disApproveEmote);
                if(ProfanityFilterService.VoteResult.isFinal(voteResult)) {
                    profanityFilterService.verifyProfanityUse(use, voteResult);
                }
            }
        }
        return DefaultListenerResult.IGNORED;
    }

    private ProfanityFilterService.VoteResult getVoteResultOnMessage(CachedMessage cachedMessage, AEmote agreementEmote, AEmote disagreementEmote) {
        Long voteThreshold = configService.getLongValueOrConfigDefault(PROFANITY_VOTES_CONFIG_KEY, cachedMessage.getServerId());
        Optional<CachedReactions> agreementReactionsOptional = emoteService.getReactionFromMessageByEmote(cachedMessage, agreementEmote);
        Optional<CachedReactions> disAgreementReactionsOptional = emoteService.getReactionFromMessageByEmote(cachedMessage, disagreementEmote);
        int agreementVotes = 0;
        int disagreementVotes = 0;
        if(agreementReactionsOptional.isPresent()) {
            agreementVotes = getUserCount(agreementReactionsOptional.get());
        }
        if(disAgreementReactionsOptional.isPresent()) {
            disagreementVotes = getUserCount(disAgreementReactionsOptional.get());
        }
        if(agreementVotes >= voteThreshold) {
            return ProfanityFilterService.VoteResult.AGREEMENT;
        } else if(disagreementVotes >= voteThreshold) {
            return ProfanityFilterService.VoteResult.DISAGREEMENT;
        } else {
            return ProfanityFilterService.VoteResult.BELOW_THRESHOLD;
        }
    }

    private int getUserCount(CachedReactions agreementReactionsOptional) {
        int reactionCount = agreementReactionsOptional.getUsers().size();
        if(agreementReactionsOptional.getSelf()) {
            reactionCount--;
        }
        return reactionCount;
    }

    @Override
    public FeatureDefinition getFeature() {
        return ProfanityFilterFeatureDefinition.PROFANITY_FILTER;
    }

}
