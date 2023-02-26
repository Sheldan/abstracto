package dev.sheldan.abstracto.suggestion.model.template;

import lombok.Builder;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.time.Instant;

@Getter
@Builder
public class SuggestionThreadModel {
    private Long suggestionId;
    private User suggester;
    private Member member;
    private String text;
    private Long serverId;
    private Instant autoEvaluationTargetDate;
    private Boolean autoEvaluationEnabled;
}
