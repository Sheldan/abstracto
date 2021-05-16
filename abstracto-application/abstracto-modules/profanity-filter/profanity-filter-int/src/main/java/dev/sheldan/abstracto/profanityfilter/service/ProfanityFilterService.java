package dev.sheldan.abstracto.profanityfilter.service;

import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.database.ProfanityRegex;
import dev.sheldan.abstracto.profanityfilter.model.database.ProfanityUse;
import dev.sheldan.abstracto.profanityfilter.model.database.ProfanityUserInAServer;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ProfanityFilterService {
    String REPORT_AGREE_EMOTE = "profanityFilterAgreeEmote";
    String PROFANITY_VOTES_CONFIG_KEY = "profanityVotes";
    String REPORT_DISAGREE_EMOTE = "profanityFilterDisagreeEmote";
    String PROFANITY_FILTER_EFFECT_KEY = "profanityFilter";

    enum VoteResult {
        AGREEMENT, DISAGREEMENT, BELOW_THRESHOLD;
        public static boolean isFinal(VoteResult result) {
            return result.equals(AGREEMENT) || result.equals(DISAGREEMENT);
        }
    }

    CompletableFuture<Void> createProfanityReport(Message message, ProfanityRegex profanityRegex);
    boolean isMessageProfanityReport(Long messageId);
    void verifyProfanityUse(ProfanityUse profanityUse, VoteResult result);
    Long getPositiveReportCountForUser(AUserInAServer aUserInAServer);
    Long getPositiveReportCountForUser(ProfanityUserInAServer aUserInAServer);
    Long getFalseProfanityReportCountForUser(AUserInAServer aUserInAServer);
    Long getFalseProfanityReportCountForUser(ProfanityUserInAServer aUserInAServer);
    List<ServerChannelMessage> getRecentPositiveReports(AUserInAServer aUserInAServer, int count);
    List<ServerChannelMessage> getRecentPositiveReports(ProfanityUserInAServer aUserInAServer, int count);
}
