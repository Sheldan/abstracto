package dev.sheldan.abstracto.suggestion.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.suggestion.model.PollCreationRequest;
import dev.sheldan.abstracto.suggestion.model.database.Poll;
import dev.sheldan.abstracto.suggestion.model.database.PollOption;
import dev.sheldan.abstracto.suggestion.repository.PollOptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PollOptionManagementServiceBean implements PollOptionManagementService {

    @Autowired
    private PollOptionRepository pollOptionRepository;

    @Override
    public void addOptionsToPoll(Poll poll, PollCreationRequest pollCreationRequest) {
        List<PollOption> options = pollCreationRequest.getOptions().stream().map(option -> PollOption
                .builder()
                .poll(poll)
                .server(poll.getServer())
                .label(option.getLabel())
                .value(option.getLabel())
                .description(option.getDescription())
                .build()).collect(Collectors.toList());
        pollOptionRepository.saveAll(options);
    }

    @Override
    public void addOptionToPoll(Poll poll, String label, String description) {
        addOptionToPoll(poll, label, description, null);
    }

    @Override
    public void addOptionToPoll(Poll poll, String label, String description, AUserInAServer adder) {
        PollOption option = PollOption
                .builder()
                .poll(poll)
                .label(label)
                .value(label)
                .server(poll.getServer())
                .adder(adder)
                .description(description)
                .build();
        pollOptionRepository.save(option);
    }

    @Override
    public Optional<PollOption> getPollOptionByName(Poll poll, String key) {
        return Optional.empty();
    }


}
