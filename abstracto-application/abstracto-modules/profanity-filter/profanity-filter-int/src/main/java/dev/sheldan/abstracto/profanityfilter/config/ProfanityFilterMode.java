package dev.sheldan.abstracto.profanityfilter.config;

import dev.sheldan.abstracto.core.config.FeatureMode;
import lombok.Getter;

@Getter
public enum ProfanityFilterMode implements FeatureMode {
    AUTO_DELETE_PROFANITIES("autoDeleteProfanities"),
    PROFANITY_VOTE("profanityVote"),
    PROFANITY_REPORT("profanityReport"),
    AUTO_DELETE_AFTER_VOTE("autoDeleteAfterVote"),
    TRACK_PROFANITIES("trackProfanities");

    private final String key;

    ProfanityFilterMode(String key) {
        this.key = key;
    }

}
