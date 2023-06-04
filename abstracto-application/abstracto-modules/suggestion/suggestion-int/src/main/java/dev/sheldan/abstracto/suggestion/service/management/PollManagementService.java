package dev.sheldan.abstracto.suggestion.service.management;

import dev.sheldan.abstracto.suggestion.model.PollCreationRequest;
import dev.sheldan.abstracto.suggestion.model.database.Poll;
import dev.sheldan.abstracto.suggestion.model.database.PollType;

import java.util.Optional;

public interface PollManagementService {
    Poll createPoll(PollCreationRequest pollCreationRequest);
    Poll getPollByPollId(Long pollId, Long serverId, PollType pollType);
    Optional<Poll> getPollByPollIdOptional(Long pollId, Long serverId, PollType pollType);
}
