package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.models.exception.GenericExceptionModel;
import dev.sheldan.abstracto.core.models.FullUser;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.templating.Templatable;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ExceptionServiceBean implements ExceptionService {

    @Autowired
    private ChannelService channelService;

    @Autowired
    private TemplateService templateService;

    @Override
    public CommandResult reportExceptionToContext(Throwable throwable, CommandContext context, Command command) {
        if(command != null && command.getConfiguration().isReportsException()) {
            try {
                FullUser fullUser = FullUser
                        .builder()
                        .aUserInAServer(context.getUserInitiatedContext().getAUserInAServer())
                        .member(context.getAuthor())
                        .build();
                GenericExceptionModel modMailExceptionModel = GenericExceptionModel
                        .builder()
                        .user(fullUser)
                        .throwable(throwable)
                        .build();
                channelService.sendEmbedTemplateInChannel("generic_command_exception", modMailExceptionModel, context.getChannel());
            } catch (Exception e) {
                log.error("Failed to notify about assignable role exception.", e);
            }
        } else if(throwable instanceof Templatable){
            Templatable exception = (Templatable) throwable;
            String text = templateService.renderTemplate(exception.getTemplateName(), exception.getTemplateModel());
            channelService.sendTextToChannel(text, context.getChannel());
        } else {
            channelService.sendTextToChannel(throwable.getLocalizedMessage(), context.getChannel());
        }
        return CommandResult.fromReportedError();
    }
}
