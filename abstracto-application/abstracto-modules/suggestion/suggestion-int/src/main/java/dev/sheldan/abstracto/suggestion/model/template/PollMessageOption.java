package dev.sheldan.abstracto.suggestion.model.template;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PollMessageOption {
    private String value;
    private String label;
    private String description;
    private Integer votes;
    private Float percentage;
}
