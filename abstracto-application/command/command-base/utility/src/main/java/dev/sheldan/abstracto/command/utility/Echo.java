package dev.sheldan.abstracto.command.utility;

import dev.sheldan.abstracto.command.Command;
import dev.sheldan.abstracto.command.HelpInfo;
import dev.sheldan.abstracto.command.execution.Configuration;
import dev.sheldan.abstracto.command.execution.CommandContext;
import dev.sheldan.abstracto.command.execution.Parameter;
import dev.sheldan.abstracto.command.execution.Result;
import dev.sheldan.abstracto.command.utility.model.EchoModel;
import dev.sheldan.abstracto.templating.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class Echo implements Command {

    private static final String TEMPLATE_NAME = "echo";

    @Autowired
    private TemplateService templateService;

    @Override
    @Transactional
    public Result execute(CommandContext commandContext) {
        StringBuilder sb = new StringBuilder();
        commandContext.getParameters().getParameters().forEach(o -> {
            sb.append(o.toString());
        });
        EchoModel model = EchoModel.parentBuilder().parent(commandContext.getCommandTemplateContext()).text(sb.toString()).build();
        commandContext.getChannel().sendMessage(templateService.renderTemplate(TEMPLATE_NAME, model)).queue();
        return Result.fromSuccess();
    }

    @Override
    public Configuration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("input").type(String.class).remainder(true).build());
        HelpInfo helpInfo = HelpInfo.builder().usage("echo <text>").longHelp("Echos back the text put in").build();
        return Configuration.builder()
                .name("echo")
                .module("utility")
                .description("Echos the input back to the same channel")
                .causesReaction(false)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }
}
