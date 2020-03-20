package dev.sheldan.abstracto.commands.management.post;

import dev.sheldan.abstracto.command.Command;
import dev.sheldan.abstracto.command.PostCommandExecution;
import dev.sheldan.abstracto.command.TemplatedException;
import dev.sheldan.abstracto.command.execution.CommandContext;
import dev.sheldan.abstracto.command.execution.Result;
import dev.sheldan.abstracto.command.execution.ResultState;
import dev.sheldan.abstracto.templating.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExceptionPostExecution implements PostCommandExecution {

    @Autowired
    private TemplateService templateService;

    @Override
    public void execute(CommandContext commandContext, Result result, Command command) {
        if(result.getResult().equals(ResultState.ERROR)) {
            if(result.getThrowable() != null) {
                log.warn("Exception", result.getThrowable());
                if(result.getThrowable() instanceof TemplatedException) {
                    TemplatedException exception = (TemplatedException) result.getThrowable();
                    String text = templateService.renderTemplate(exception.getTemplateName(), exception.getTemplateModel());
                    commandContext.getChannel().sendMessage(text).queue();
                } else {
                    commandContext.getChannel().sendMessage("Exception: " + result.getThrowable().getClass() + ": " + result.getMessage()).queue();
                }
            }
        }
    }
}
