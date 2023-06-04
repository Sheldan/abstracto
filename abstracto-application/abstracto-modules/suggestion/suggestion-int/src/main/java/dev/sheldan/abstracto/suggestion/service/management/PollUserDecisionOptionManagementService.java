package dev.sheldan.abstracto.suggestion.service.management;

import dev.sheldan.abstracto.suggestion.model.database.PollOption;
import dev.sheldan.abstracto.suggestion.model.database.PollUserDecision;
import dev.sheldan.abstracto.suggestion.model.database.PollUserDecisionOption;

import java.util.List;

public interface PollUserDecisionOptionManagementService {
    PollUserDecisionOption addDecisionForUser(PollUserDecision decision, PollOption pollOption);
    void clearOptions(PollUserDecision pollUserDecision);
    void deleteDecisionOptions(PollUserDecision decision, List<PollUserDecisionOption> decisionOptionList);
}
