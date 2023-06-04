package dev.sheldan.abstracto.suggestion.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.suggestion.model.database.Poll;
import dev.sheldan.abstracto.suggestion.model.database.PollUserDecision;

import java.util.Optional;

public interface PollUserDecisionManagementService {
    PollUserDecision addUserDecision(Poll poll, AUserInAServer user);
    PollUserDecision createUserDecision(Poll poll, AUserInAServer user);
    Optional<PollUserDecision> getUserDecisionOptional(Poll poll, AUserInAServer user);
    PollUserDecision getUserDecision(Poll poll, AUserInAServer user);
    void savePollUserDecision(PollUserDecision pollUserDecision);
}
