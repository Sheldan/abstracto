package dev.sheldan.abstracto.suggestion.model.template;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
public class PollInfoModel {
    private Long id;
    private String description;
    private Boolean allowMultiple;
    private Boolean showDecisions;
    private Boolean allowAdditions;
    private Instant creationDate;
    private Integer totalVotes;
    private Instant targetDate;
    private Duration pollDuration;
    private List<PollOptionInfoModel> options;
}
