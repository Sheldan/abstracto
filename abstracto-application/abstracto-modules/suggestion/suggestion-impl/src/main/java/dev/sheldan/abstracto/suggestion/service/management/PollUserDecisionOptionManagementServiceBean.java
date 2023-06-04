package dev.sheldan.abstracto.suggestion.service.management;

import dev.sheldan.abstracto.suggestion.model.database.PollOption;
import dev.sheldan.abstracto.suggestion.model.database.PollUserDecision;
import dev.sheldan.abstracto.suggestion.model.database.PollUserDecisionOption;
import dev.sheldan.abstracto.suggestion.repository.PollUserDecisionOptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PollUserDecisionOptionManagementServiceBean implements PollUserDecisionOptionManagementService {

    @Autowired
    private PollUserDecisionOptionRepository repository;

    @Override
    public PollUserDecisionOption addDecisionForUser(PollUserDecision decision, PollOption pollOption) {
        PollUserDecisionOption option = PollUserDecisionOption
                .builder()
                .decision(decision)
                .poll(decision.getPoll())
                .pollOption(pollOption)
                .build();
        decision.getOptions().add(option);
        return option;
    }

    @Override
    public void clearOptions(PollUserDecision pollUserDecision) {
        repository.deleteAll(pollUserDecision.getOptions());
    }

    @Override
    public void deleteDecisionOptions(PollUserDecision decision, List<PollUserDecisionOption> decisionOptionList) {
        decision.getOptions().removeAll(decisionOptionList);
        repository.deleteAll(decisionOptionList);
    }

}
