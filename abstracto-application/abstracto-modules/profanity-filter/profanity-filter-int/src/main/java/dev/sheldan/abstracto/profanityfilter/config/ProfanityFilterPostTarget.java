package dev.sheldan.abstracto.profanityfilter.config;

import dev.sheldan.abstracto.core.config.PostTargetEnum;
import lombok.Getter;

@Getter
public enum ProfanityFilterPostTarget implements PostTargetEnum {
    PROFANITY_FILTER_QUEUE("profanityQueue");

    private String key;

    ProfanityFilterPostTarget(String key) {
        this.key = key;
    }
}

