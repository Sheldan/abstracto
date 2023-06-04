package dev.sheldan.abstracto.suggestion.model.template;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class QuickPollEvaluationModel {
    private Long pollId;
    private String description;
    private Long pollMessageId;
    private List<PollMessageOption> topOptions;
    private List<PollMessageOption> options;
}
