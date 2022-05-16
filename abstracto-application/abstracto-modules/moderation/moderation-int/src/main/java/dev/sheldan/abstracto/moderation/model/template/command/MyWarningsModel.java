package dev.sheldan.abstracto.moderation.model.template.command;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

/**
 * Used to render the response of the myWarnings command. The template is: 'myWarnings_response_embed'
 */
@Getter
@Setter
@Builder
public class MyWarningsModel {
    /**
     * The total amount of warnings the member has
     */
    private Long totalWarnCount;
    /**
     * The current (only active) amount of warnings the member has
     */
    private Long currentWarnCount;
    private Member member;
}
