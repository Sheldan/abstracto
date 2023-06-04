package dev.sheldan.abstracto.suggestion.model.template;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PollAddOptionModalModel {
    private String modalId;
    private String labelInputComponentId;
    private String descriptionInputComponentId;
}
