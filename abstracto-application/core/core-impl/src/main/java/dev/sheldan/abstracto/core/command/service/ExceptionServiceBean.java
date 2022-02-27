package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.model.exception.GenericExceptionModel;
import dev.sheldan.abstracto.core.interaction.GenericInteractionExceptionModel;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.listener.async.jda.ButtonClickedListener;
import dev.sheldan.abstracto.core.models.FullUser;
import dev.sheldan.abstracto.core.models.FullUserInServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.listener.ButtonClickedListenerModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserManagementService;
import dev.sheldan.abstracto.core.templating.Templatable;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
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

    @Autowired
    private InteractionService interactionService;

    @Override
    public CommandResult reportExceptionToContext(Throwable throwable, CommandContext context, Command command) {
        if(command != null) {
            log.info("Reporting generic exception {} of command {} towards channel {} in server {}.",
                    throwable.getClass().getSimpleName(), command.getConfiguration().getName(), context.getChannel().getId(), context.getGuild().getId());
        } else {
            log.info("Reporting generic exception {} towards channel {} in server {}.",
                    throwable.getClass().getSimpleName(), context.getChannel().getId(), context.getGuild().getId());
        }
        if((command != null && command.getConfiguration().isSupportsEmbedException()) || throwable instanceof Templatable) {
            try {
                reportGenericException(throwable, context);
            } catch (Exception e) {
                log.error("Failed to notify about exception.", e);
            }
        } else {
            channelService.sendTextToChannel(throwable.getLocalizedMessage(), context.getChannel());
        }
        return CommandResult.fromReportedError();
    }

    @Override
    public void reportExceptionToInteraction(Throwable exception, ButtonClickedListenerModel interactionContext, ButtonClickedListener executedListener) {
        if(executedListener != null) {
            log.info("Reporting generic exception {} of listener {} towards channel {} in server {}.",
                    exception.getClass().getSimpleName(), executedListener.getClass().getSimpleName(), interactionContext.getEvent().getChannel().getIdLong(),
                    interactionContext.getEvent().getGuild().getIdLong());
        } else {
            log.info("Reporting generic exception {} towards channel {} in server {}.",
                    exception.getClass().getSimpleName(), interactionContext.getEvent().getChannel().getIdLong(),
                    interactionContext.getEvent().getGuild().getIdLong());
        }
        try {
            reportGenericInteractionException(exception, interactionContext);
        } catch (Exception e) {
            log.error("Failed to notify about exception.", e);
        }
    }

    private void reportGenericException(Throwable throwable, CommandContext context) {
        GenericExceptionModel exceptionModel = buildCommandModel(throwable, context);
        channelService.sendEmbedTemplateInTextChannelList("generic_command_exception", exceptionModel, context.getChannel());
    }

    private void reportGenericInteractionException(Throwable throwable, ButtonClickedListenerModel interactionContext) {
        GenericInteractionExceptionModel exceptionModel = buildInteractionExceptionModel(throwable, interactionContext);
        interactionService.sendMessageToInteraction("generic_interaction_exception", exceptionModel, interactionContext.getEvent().getInteraction().getHook());
    }

    @Override
    public void reportExceptionToGuildMessageReceivedContext(Throwable exception, MessageReceivedEvent event) {
        if(exception instanceof Templatable){
            GenericExceptionModel model = buildMemberContext(exception, event.getMember());
            String text = templateService.renderTemplate(MODEL_WRAPPER_TEMPLATE_KEY, model);
            channelService.sendTextToChannel(text, event.getChannel());
        } else {
            channelService.sendTextToChannel(exception.getLocalizedMessage(), event.getChannel());
        }
    }

    @Override
    public void reportExceptionToPrivateMessageReceivedContext(Throwable exception, MessageReceivedEvent event) {
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

    private GenericInteractionExceptionModel buildInteractionExceptionModel(Throwable throwable, ButtonClickedListenerModel context) {
        return GenericInteractionExceptionModel
                .builder()
                .member(context.getEvent().getMember())
                .user(context.getEvent().getUser())
                .throwable(throwable)
                .build();
    }
    private GenericExceptionModel buildCommandModel(Throwable throwable, CommandContext context) {
        FullUserInServer fullUser = FullUserInServer.builder().member(context.getAuthor()).aUserInAServer(userInServerManagementService.loadUserOptional(context.getGuild().getIdLong(), context.getAuthor().getIdLong()).orElse(null)).build();
        return GenericExceptionModel
                .builder()
                .user(fullUser)
                .throwable(throwable)
                .build();
    }

    private GenericExceptionModel buildMemberContext(Throwable throwable, Member member) {
        AUserInAServer userInAServer = userInServerManagementService.loadOrCreateUser(member);
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
        AUser aUser = userManagementService.loadOrCreateUser(user.getIdLong());
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
