package dev.sheldan.abstracto.suggestion.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.suggestion.model.database.Suggestion;
import dev.sheldan.abstracto.suggestion.model.database.SuggestionDecision;
import dev.sheldan.abstracto.suggestion.model.database.SuggestionVote;
import dev.sheldan.abstracto.suggestion.model.database.embed.SuggestionVoterId;
import dev.sheldan.abstracto.suggestion.repository.SuggestionVoteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class SuggestionVoteManagementServiceBean implements SuggestionVoteManagementService {

    @Autowired
    private SuggestionVoteRepository suggestionVoteRepository;

    @Override
    public Optional<SuggestionVote> getSuggestionVote(AUserInAServer aUserInAServer, Suggestion suggestion) {
        SuggestionVoterId suggestionVoteId = SuggestionVoterId
                .builder()
                .suggestionId(suggestion.getSuggestionId().getId())
                .serverId(suggestion.getServer().getId())
                .voterId(aUserInAServer.getUserInServerId())
                .build();
        return suggestionVoteRepository.findById(suggestionVoteId);
    }

    @Override
    public void deleteSuggestionVote(AUserInAServer aUserInAServer, Suggestion suggestion) {
        Optional<SuggestionVote> voteOptional = getSuggestionVote(aUserInAServer, suggestion);
        voteOptional.ifPresent(suggestionVote -> suggestionVoteRepository.delete(suggestionVote));

        if(!voteOptional.isPresent()) {
            log.warn("User {} in server {} did not have a vote for suggestion {}.",
                    aUserInAServer.getUserReference().getId(), aUserInAServer.getServerReference().getId(), suggestion.getSuggestionId().getId());
        }
    }

    @Override
    public SuggestionVote createSuggestionVote(AUserInAServer aUserInAServer, Suggestion suggestion, SuggestionDecision decision) {
        SuggestionVoterId suggestionVoteId = SuggestionVoterId
                .builder()
                .suggestionId(suggestion.getSuggestionId().getId())
                .serverId(suggestion.getServer().getId())
                .voterId(aUserInAServer.getUserInServerId())
                .build();
        SuggestionVote vote = SuggestionVote
                .builder()
                .suggestionVoteId(suggestionVoteId)
                .voter(aUserInAServer)
                .decision(decision)
                .suggestion(suggestion)
                .build();
        log.info("Creating suggestion decision of user {} on suggestion {} in server {} to {}.", aUserInAServer.getUserReference().getId(),
                suggestion.getSuggestionId().getId(), suggestion.getServer().getId(), decision);
        return suggestionVoteRepository.save(vote);
    }

    @Override
    public Long getDecisionsForSuggestion(Suggestion suggestion, SuggestionDecision decision) {
        return suggestionVoteRepository.countByDecisionAndSuggestionVoteId_SuggestionIdAndSuggestionVoteId_ServerId(decision, suggestion.getSuggestionId().getId(), suggestion.getSuggestionId().getServerId());
    }

    @Override
    public void deleteSuggestionVotes(Suggestion suggestion) {
        suggestionVoteRepository.deleteBySuggestionVoteId_SuggestionIdAndSuggestionVoteId_ServerId(suggestion.getSuggestionId().getId(), suggestion.getSuggestionId().getServerId());
    }
}
