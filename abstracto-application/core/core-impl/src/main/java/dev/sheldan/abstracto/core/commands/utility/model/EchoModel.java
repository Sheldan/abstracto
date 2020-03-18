package dev.sheldan.abstracto.core.commands.utility.model;


import dev.sheldan.abstracto.command.execution.CommandTemplateContext;
import lombok.Builder;
import lombok.Getter;

@Getter
public class EchoModel extends CommandTemplateContext {
    private String text;

    @Builder(builderMethodName = "parentBuilder")
    private EchoModel(CommandTemplateContext parent, String text) {
        super(parent);
        this.text = text;
    }
}
