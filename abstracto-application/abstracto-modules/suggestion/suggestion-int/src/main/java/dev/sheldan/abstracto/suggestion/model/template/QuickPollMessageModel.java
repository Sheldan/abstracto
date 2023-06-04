package dev.sheldan.abstracto.suggestion.model.template;

import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
public class QuickPollMessageModel {
    private MemberDisplay creator;
    private Long pollId;
    private String description;
    private String selectionMenuId;
    private String addOptionButtonId;
    private Boolean allowMultiple;
    private Instant endDate;
    private Boolean showDecisions;
    private List<PollMessageOption> options;
}
