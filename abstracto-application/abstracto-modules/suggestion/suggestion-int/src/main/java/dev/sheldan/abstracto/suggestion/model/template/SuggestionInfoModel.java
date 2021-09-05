package dev.sheldan.abstracto.suggestion.model.template;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SuggestionInfoModel {
    private Long agreements;
    private Long disagreements;
}
