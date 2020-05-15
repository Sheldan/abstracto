package dev.sheldan.abstracto.utility.config.posttargets;

import dev.sheldan.abstracto.core.config.PostTargetEnum;
import lombok.Getter;

@Getter
public enum SuggestionPostTarget implements PostTargetEnum {
    SUGGESTION("suggestions");

    private String key;

    SuggestionPostTarget(String key) {
        this.key = key;
    }
}
