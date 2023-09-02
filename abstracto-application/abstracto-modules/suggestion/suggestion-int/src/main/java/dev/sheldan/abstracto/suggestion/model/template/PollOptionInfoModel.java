package dev.sheldan.abstracto.suggestion.model.template;

import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PollOptionInfoModel {
    private String value;
    private String label;
    private String description;
    private Long votes;
    private Float percentage;
    private MemberDisplay adder;
}
