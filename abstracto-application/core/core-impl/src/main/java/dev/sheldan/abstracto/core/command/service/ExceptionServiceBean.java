package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.models.exception.GenericExceptionModel;
import dev.sheldan.abstracto.core.models.FullUser;
import dev.sheldan.abstracto.core.models.FullUserInServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserManagementService;
import dev.sheldan.abstracto.templating.Templatable;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ExceptionServiceBean implements ExceptionService {

    public static final String MODEL_WRAPPER_TEMPLATE_KEY = "model_wrapper";
    @Autowired
    private ChannelService channelService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private UserManagementService userManagementService;

    @Override
    public CommandResult reportExceptionToContext(Throwable throwable, CommandContext context, Command command) {
        if(command != null && command.getConfiguration().isSupportsEmbedException()) {
            try {
                GenericExceptionModel exceptionModel = buildCommandModel(throwable, context);
                log.info("Reporting generic exception {} of command {} towards channel {} in server {}.",
                        throwable.getClass().getSimpleName(), command.getConfiguration().getName(), context.getChannel().getId(), context.getGuild().getId());
                channelService.sendEmbedTemplateInChannel("generic_command_exception", exceptionModel, context.getChannel());
            } catch (Exception e) {
                log.error("Failed to notify about exception.", e);
            }
        } else if(throwable instanceof Templatable){
            GenericExceptionModel exceptionModel = buildCommandModel(throwable, context);
            String text = templateService.renderTemplate(MODEL_WRAPPER_TEMPLATE_KEY, exceptionModel);
            channelService.sendTextToChannel(text, context.getChannel());
        } else {
            channelService.sendTextToChannel(throwable.getLocalizedMessage(), context.getChannel());
        }
        return CommandResult.fromReportedError();
    }

    @Override
    public void reportExceptionToGuildMessageReceivedContext(Throwable exception, GuildMessageReceivedEvent event) {
        if(exception instanceof Templatable){
            GenericExceptionModel model = buildMemberContext(exception, event.getMember());
            String text = templateService.renderTemplate(MODEL_WRAPPER_TEMPLATE_KEY, model);
            channelService.sendTextToChannel(text, event.getChannel());
        } else {
            channelService.sendTextToChannel(exception.getLocalizedMessage(), event.getChannel());
        }
    }

    @Override
    public void reportExceptionToPrivateMessageReceivedContext(Throwable exception, PrivateMessageReceivedEvent event) {
        if(exception instanceof Templatable){
            GenericExceptionModel model = buildPrivateMessageReceivedModel(exception, event.getAuthor());
            String text = templateService.renderTemplate(MODEL_WRAPPER_TEMPLATE_KEY, model);
            channelService.sendTextToChannel(text, event.getChannel());
        } else {
            channelService.sendTextToChannel(exception.getLocalizedMessage(), event.getChannel());
        }
    }

    @Override
    public void reportExceptionToChannel(Throwable exception, MessageChannel channel, Member member) {
        if(exception instanceof Templatable){
            GenericExceptionModel model = buildMemberContext(exception, member);
            String text = templateService.renderTemplate(MODEL_WRAPPER_TEMPLATE_KEY, model);
            channelService.sendTextToChannel(text, channel);
        } else {
            channelService.sendTextToChannel(exception.getLocalizedMessage(), channel);
        }
    }

    private GenericExceptionModel buildCommandModel(Throwable throwable, CommandContext context) {
        FullUserInServer fullUser = FullUserInServer.builder().member(context.getAuthor()).aUserInAServer(context.getUserInitiatedContext().getAUserInAServer()).build();
        return GenericExceptionModel
                .builder()
                .user(fullUser)
                .throwable(throwable)
                .build();
    }

    private GenericExceptionModel buildMemberContext(Throwable throwable, Member member) {
        AUserInAServer userInAServer = userInServerManagementService.loadUser(member);
        FullUserInServer fullUser = FullUserInServer
                .builder()
                .aUserInAServer(userInAServer)
                .member(member)
                .build();
        return GenericExceptionModel
                .builder()
                .user(fullUser)
                .throwable(throwable)
                .build();
    }

    private GenericExceptionModel buildPrivateMessageReceivedModel(Throwable throwable, User user) {
        AUser aUser = userManagementService.loadUser(user.getIdLong());
        FullUser fullUser = FullUser
                .builder()
                .user(user)
                .auser(aUser)
                .build();
        return GenericExceptionModel
                .builder()
                .fullUser(fullUser)
                .throwable(throwable)
                .build();
    }
}
