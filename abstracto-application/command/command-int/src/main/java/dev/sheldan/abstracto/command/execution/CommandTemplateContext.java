package dev.sheldan.abstracto.command.execution;

import dev.sheldan.abstracto.core.models.AChannel;
import dev.sheldan.abstracto.core.models.AServer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder @AllArgsConstructor
public class CommandTemplateContext {
    private AChannel channel;
    private AServer server;

    public CommandTemplateContext(CommandTemplateContext commandTemplateContext) {
        this.channel = commandTemplateContext.channel;
        this.server = commandTemplateContext.server;
    }
}
