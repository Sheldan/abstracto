package dev.sheldan.abstracto.suggestion.model.template;

import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.suggestion.model.database.Poll;
import dev.sheldan.abstracto.suggestion.model.database.PollState;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
public class ServerPollMessageModel {
    private MemberDisplay creator;
    private Long pollId;
    private PollState state;
    private String description;
    private String selectionMenuId;
    private String addOptionButtonId;
    private Boolean allowMultiple;
    private Boolean showDecisions;
    private Boolean allowAdditions;
    private Instant endDate;
    private List<PollMessageOption> options;

    public static ServerPollMessageModel fromPoll(Poll poll, List<PollMessageOption> options) {
        return ServerPollMessageModel
                .builder()
                .creator(MemberDisplay.fromAUserInAServer(poll.getCreator()))
                .description(poll.getDescription())
                .pollId(poll.getId())
                .state(poll.getState())
                .allowMultiple(poll.getAllowMultiple())
                .showDecisions(poll.getShowDecisions())
                .endDate(poll.getTargetDate())
                .allowAdditions(poll.getAllowAddition())
                .options(options)
                .addOptionButtonId(poll.getAddOptionButtonId())
                .selectionMenuId(poll.getSelectionMenuId())
                .build();
    }
}
