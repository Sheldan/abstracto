package dev.sheldan.abstracto.suggestion.service.management;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.suggestion.model.database.Poll;
import dev.sheldan.abstracto.suggestion.model.database.PollUserDecision;
import dev.sheldan.abstracto.suggestion.repository.PollUserDecisionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Optional;

@Component
public class PollUserDecisionManagementServiceBean implements PollUserDecisionManagementService {

    @Autowired
    private PollUserDecisionRepository repository;

    @Override
    public PollUserDecision addUserDecision(Poll poll, AUserInAServer user) {
        return repository.save(createUserDecision(poll, user));
    }

    @Override
    public PollUserDecision createUserDecision(Poll poll, AUserInAServer user) {
        return PollUserDecision
                .builder()
                .server(user.getServerReference())
                .voter(user)
                .options(new ArrayList<>())
                .poll(poll)
                .build();
    }

    @Override
    public Optional<PollUserDecision> getUserDecisionOptional(Poll poll, AUserInAServer user) {
        return repository.findPollUserDecisionByPollAndVoter(poll, user);
    }

    @Override
    public PollUserDecision getUserDecision(Poll poll, AUserInAServer user) {
        return repository.findPollUserDecisionByPollAndVoter(poll, user).orElseThrow(() -> new AbstractoRunTimeException("User decision not found."));
    }

    @Override
    public void savePollUserDecision(PollUserDecision pollUserDecision) {
        repository.save(pollUserDecision);
    }

}
