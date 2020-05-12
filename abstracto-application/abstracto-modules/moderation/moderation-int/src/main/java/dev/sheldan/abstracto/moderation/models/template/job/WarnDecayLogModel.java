package dev.sheldan.abstracto.moderation.models.template.job;

import dev.sheldan.abstracto.core.models.context.ServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Used when rendering the log message when warnings were decayed. The template is: "warn_decay_log_embed"
 */
@Getter
@Setter
@SuperBuilder
public class WarnDecayLogModel extends ServerContext {
    /**
     * The warnings which were decayed
     */
    private List<WarnDecayWarning> warnings;
}
