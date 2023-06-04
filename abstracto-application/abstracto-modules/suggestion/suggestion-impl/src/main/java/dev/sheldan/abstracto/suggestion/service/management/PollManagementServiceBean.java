package dev.sheldan.abstracto.suggestion.service.management;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.suggestion.exception.PollNotFoundException;
import dev.sheldan.abstracto.suggestion.model.PollCreationRequest;
import dev.sheldan.abstracto.suggestion.model.database.Poll;
import dev.sheldan.abstracto.suggestion.model.database.PollState;
import dev.sheldan.abstracto.suggestion.model.database.PollType;
import dev.sheldan.abstracto.suggestion.repository.PollRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
@Slf4j
public class PollManagementServiceBean implements PollManagementService {

    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Override
    public Poll createPoll(PollCreationRequest pollCreationRequest) {
        ServerUser creatorServerUser = ServerUser
                .builder()
                .userId(pollCreationRequest.getCreatorId())
                .serverId(pollCreationRequest.getServerId())
                .build();
        AUserInAServer creator = userInServerManagementService.loadOrCreateUser(creatorServerUser);
        AChannel channel = channelManagementService.loadChannel(pollCreationRequest.getPollChannelId());
        Poll pollInstance = Poll
                .builder()
                .description(pollCreationRequest.getDescription())
                .server(creator.getServerReference())
                .pollId(pollCreationRequest.getPollId())
                .allowMultiple(pollCreationRequest.getAllowMultiple())
                .allowAddition(pollCreationRequest.getAllowAddition())
                .showDecisions(pollCreationRequest.getShowDecisions())
                .reminderJobTriggerKey(pollCreationRequest.getReminderJobTrigger())
                .targetDate(pollCreationRequest.getTargetDate())
                .evaluationJobTriggerKey(pollCreationRequest.getEvaluationJobTrigger())
                .messageId(pollCreationRequest.getPollMessageId())
                .channel(channel)
                .addOptionButtonId(pollCreationRequest.getAddOptionButtonId())
                .selectionMenuId(pollCreationRequest.getSelectionMenuId())
                .creator(creator)
                .state(PollState.NEW)
                .type(pollCreationRequest.getType())
                .build();
        return pollRepository.save(pollInstance);
    }

    @Override
    public Poll getPollByPollId(Long pollId, Long serverId, PollType pollType) {
        return getPollByPollIdOptional(pollId, serverId, pollType).orElseThrow(() -> new PollNotFoundException(pollId));
    }

    @Override
    public Optional<Poll> getPollByPollIdOptional(Long pollId, Long serverId, PollType pollType) {
        return pollRepository.findByPollIdAndServer_IdAndType(pollId, serverId, pollType);
    }

}
