package dev.sheldan.abstracto.core.templating.model;

import java.time.Instant;

public interface EffectiveTemplate {
    String getKey();

    String getContent();

    Instant getLastModified();
}
