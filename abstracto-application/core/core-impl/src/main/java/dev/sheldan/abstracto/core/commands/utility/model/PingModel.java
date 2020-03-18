package dev.sheldan.abstracto.core.commands.utility.model;

import dev.sheldan.abstracto.command.execution.CommandTemplateContext;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PingModel extends CommandTemplateContext {
    private Long latency;

    @Builder(builderMethodName = "parentBuilder")
    private PingModel(CommandTemplateContext parent, Long latency) {
        super(parent);
        this.latency = latency;
    }
}
