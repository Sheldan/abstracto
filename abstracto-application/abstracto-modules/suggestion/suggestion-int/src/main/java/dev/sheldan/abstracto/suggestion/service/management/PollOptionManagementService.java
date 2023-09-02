package dev.sheldan.abstracto.suggestion.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.suggestion.model.PollCreationRequest;
import dev.sheldan.abstracto.suggestion.model.database.Poll;
import dev.sheldan.abstracto.suggestion.model.database.PollOption;

import java.util.Optional;

public interface PollOptionManagementService {
    void addOptionsToPoll(Poll poll, PollCreationRequest pollCreationRequest);
    void addOptionToPoll(Poll poll, String label, String description);
    void addOptionToPoll(Poll poll, String label, String description, AUserInAServer adder);
    Optional<PollOption> getPollOptionByName(Poll poll, String key);
}
