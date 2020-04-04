package dev.sheldan.abstracto.core.commands.utility;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.CommandCondition;
import dev.sheldan.abstracto.core.command.HelpInfo;
import dev.sheldan.abstracto.core.command.execution.CommandConfiguration;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.config.AbstractoFeatures;
import dev.sheldan.abstracto.core.models.command.EchoModel;
import dev.sheldan.abstracto.templating.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class Echo implements Command {

    private static final String TEMPLATE_NAME = "echo_response";

    @Autowired
    private TemplateService templateService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        StringBuilder sb = new StringBuilder();
        commandContext.getParameters().getParameters().forEach(o -> {
            sb.append(o.toString());
        });
        EchoModel model = EchoModel.builder().text(sb.toString()).build();
        commandContext.getChannel().sendMessage(templateService.renderTemplate(TEMPLATE_NAME, model)).queue();
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("input").type(String.class).remainder(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("echo")
                .module("utility")
                .templated(true)
                .causesReaction(false)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public String getFeature() {
        return AbstractoFeatures.CORE;
    }
}
