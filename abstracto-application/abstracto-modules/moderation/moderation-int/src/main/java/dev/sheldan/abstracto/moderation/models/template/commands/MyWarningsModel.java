package dev.sheldan.abstracto.moderation.models.template.commands;

import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Used to render the response of the myWarnings command. The template is: 'myWarnings_response_embed'
 */
@Getter
@Setter
@SuperBuilder
public class MyWarningsModel extends UserInitiatedServerContext {
    /**
     * The total amount of warnings the member has
     */
    private Long totalWarnCount;
    /**
     * The current (only active) amount of warnings the member has
     */
    private Long currentWarnCount;
}
