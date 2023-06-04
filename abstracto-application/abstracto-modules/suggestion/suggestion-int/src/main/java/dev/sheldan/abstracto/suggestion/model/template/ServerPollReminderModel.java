package dev.sheldan.abstracto.suggestion.model.template;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class ServerPollReminderModel {
    private Long pollId;
    private String description;
    private String messageLink;
    private Long pollMessageId;
    private List<PollMessageOption> topOptions;
    private List<PollMessageOption> options;
}
