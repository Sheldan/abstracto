package dev.sheldan.abstracto.suggestion.model;

import dev.sheldan.abstracto.suggestion.model.database.PollType;
import dev.sheldan.abstracto.suggestion.model.template.PollMessageOption;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Builder
@Getter
public class PollCreationRequest {
    private Long pollId;
    private String description;
    private List<PollMessageOption> options;
    private Boolean allowMultiple;
    private Boolean allowAddition;
    private Boolean showDecisions;
    private String evaluationJobTrigger;
    private String reminderJobTrigger;
    private String addOptionButtonId;
    private Instant targetDate;
    private String selectionMenuId;
    private Long serverId;
    @Setter
    private Long pollChannelId;
    private Long creatorId;
    @Setter
    private Long pollMessageId;
    private PollType type;
}
