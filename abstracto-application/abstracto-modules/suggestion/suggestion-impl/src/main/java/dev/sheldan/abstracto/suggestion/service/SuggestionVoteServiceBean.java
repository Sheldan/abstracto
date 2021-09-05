package dev.sheldan.abstracto.suggestion.service;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.suggestion.model.database.Suggestion;
import dev.sheldan.abstracto.suggestion.model.database.SuggestionDecision;
import dev.sheldan.abstracto.suggestion.model.database.SuggestionVote;
import dev.sheldan.abstracto.suggestion.service.management.SuggestionManagementService;
import dev.sheldan.abstracto.suggestion.service.management.SuggestionVoteManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class SuggestionVoteServiceBean implements SuggestionVoteService {

    @Autowired
    private SuggestionVoteManagementService suggestionVoteManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private SuggestionManagementService suggestionManagementService;

    @Override
    public SuggestionVote upsertSuggestionVote(Member votingMember, SuggestionDecision decision, Long suggestionId) {
        Long serverId = votingMember.getGuild().getIdLong();
        Suggestion suggestion = suggestionManagementService.getSuggestion(serverId, suggestionId);
        return upsertSuggestionVote(votingMember, decision, suggestion);
    }

    @Override
    public SuggestionVote upsertSuggestionVote(Member votingMember, SuggestionDecision decision, Suggestion suggestion) {
        AUserInAServer votingUser = userInServerManagementService.loadOrCreateUser(votingMember);
        Optional<SuggestionVote> suggestionVoteOptional = suggestionVoteManagementService.getSuggestionVote(votingUser, suggestion);
        if(decision.equals(SuggestionDecision.REMOVE_VOTE)) {
            deleteSuggestionVote(votingMember, suggestion);
            return null;
        }
        if(suggestionVoteOptional.isPresent()) {
            log.info("Updating suggestion decision of user {} on suggestion {} in server {} to {}.", votingMember.getIdLong(),
                    suggestion.getSuggestionId().getId(), suggestion.getServer().getId(), decision);
            SuggestionVote updatedVote = suggestionVoteOptional.get();
            updatedVote.setDecision(decision);
            return updatedVote;
        } else {
            return suggestionVoteManagementService.createSuggestionVote(votingUser, suggestion, decision);
        }
    }

    @Override
    public void deleteSuggestionVote(Member votingMember, Long suggestionId) {
        Suggestion suggestion = suggestionManagementService.getSuggestion(votingMember.getGuild().getIdLong(), suggestionId);
        deleteSuggestionVote(votingMember, suggestion);
    }

    @Override
    public void deleteSuggestionVote(Member votingMember, Suggestion suggestion) {
        AUserInAServer votingUser = userInServerManagementService.loadOrCreateUser(votingMember);
        log.info("Removing suggestion vote from user {} on suggestion {} in server {}.",
            votingMember.getIdLong(), suggestion.getSuggestionId().getId(), suggestion.getServer().getId());
        suggestionVoteManagementService.deleteSuggestionVote(votingUser, suggestion);
    }

}
