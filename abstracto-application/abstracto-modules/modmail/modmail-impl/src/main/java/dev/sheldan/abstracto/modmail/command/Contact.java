package dev.sheldan.abstracto.modmail.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.EntityGuildMismatchException;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.display.MemberNameDisplay;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.modmail.config.ModMailFeatureDefinition;
import dev.sheldan.abstracto.modmail.config.ModMailSlashCommandNames;
import dev.sheldan.abstracto.modmail.model.database.ModMailThread;
import dev.sheldan.abstracto.modmail.model.template.ModMailThreadExistsModel;
import dev.sheldan.abstracto.modmail.service.ModMailThreadService;
import dev.sheldan.abstracto.modmail.service.management.ModMailThreadManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This command is used to create a thread with a member directly. If a thread already exists, this will post a link to
 * the {@link net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel}
 */
@Component
@Slf4j
public class Contact extends AbstractConditionableCommand {

    private static final String COMMAND_NAME = "contact";
    private static final String USER_PARMETER = "user";
    private static final String MODMAIL_THREAD_ALREADY_EXISTS_TEMPLATE = "modmail_thread_already_exists";
    private static final String CONTACT_RESPONSE = "contact_response";

    @Autowired
    private ModMailThreadService modMailThreadService;

    @Autowired
    private ModMailThreadManagementService modMailThreadManagementService;

    @Autowired
    private UserInServerManagementService userManagementService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        Member targetUser = (Member) commandContext.getParameters().getParameters().get(0);
        if(!targetUser.getGuild().equals(commandContext.getGuild())) {
            throw new EntityGuildMismatchException();
        }
        AUserInAServer user = userManagementService.loadOrCreateUser(targetUser);
        // if this AUserInAServer already has an open thread, we should instead post a message
        // containing a link to the channel, instead of opening a new one
        if(modMailThreadManagementService.hasOpenModMailThreadForUser(user)) {
            log.info("Modmail thread for user {} in server {} already exists. Notifying user {}.", commandContext.getAuthor().getId(), commandContext.getGuild().getId(), user.getUserReference().getId());
            ModMailThread existingThread = modMailThreadManagementService.getOpenModMailThreadForUser(user);
            ModMailThreadExistsModel model = ModMailThreadExistsModel
                    .builder()
                    .existingModMailThread(existingThread)
                    .executingMemberDisplay(MemberNameDisplay.fromMember(targetUser))
                    .build();
            List<CompletableFuture<Message>> futures = channelService.sendEmbedTemplateInTextChannelList(MODMAIL_THREAD_ALREADY_EXISTS_TEMPLATE, model, commandContext.getChannel());
            return FutureUtils.toSingleFutureGeneric(futures).thenApply(aVoid -> CommandResult.fromIgnored());
        } else {
            return modMailThreadService.createModMailThreadForUser(targetUser.getUser(), targetUser.getGuild(), null,  false, commandContext.getUndoActions(), false)
                    .thenCompose(unused -> modMailThreadService.sendContactNotification(targetUser.getUser(), unused, commandContext.getChannel()))
                    .thenApply(aVoid -> CommandResult.fromSuccess());
        }
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        User user = slashCommandParameterService.getCommandOption(USER_PARMETER, event, User.class);
        AUserInAServer userInAServer = userManagementService.loadOrCreateUser(event.getGuild().getIdLong(), user.getIdLong());
        // if this AUserInAServer already has an open thread, we should instead post a message
        // containing a link to the channel, instead of opening a new one
        if(modMailThreadManagementService.hasOpenModMailThreadForUser(userInAServer)) {
            log.info("Modmail thread for userInAServer {} in server {} already exists. Notifying userInAServer {}.", event.getMember().getId(), event.getGuild().getId(), userInAServer.getUserReference().getId());
            ModMailThread existingThread = modMailThreadManagementService.getOpenModMailThreadForUser(userInAServer);
            ModMailThreadExistsModel model = ModMailThreadExistsModel
                    .builder()
                    .existingModMailThread(existingThread)
                    .executingMemberDisplay(MemberNameDisplay.fromMember(event.getMember()))
                    .build();
            return interactionService.replyEmbed(MODMAIL_THREAD_ALREADY_EXISTS_TEMPLATE, model, event)
                    .thenApply(interactionHook -> CommandResult.fromSuccess());
        } else {
            CompletableFuture<InteractionHook> response = interactionService.replyEmbed(CONTACT_RESPONSE, event);
            CompletableFuture<MessageChannel> threadFuture = modMailThreadService.createModMailThreadForUser(user, event.getGuild(), null, false, new ArrayList<>(), false);
            return CompletableFuture.allOf(response, threadFuture)
                    .thenCompose(unused -> modMailThreadService.sendContactNotification(user, threadFuture.join(), response.join()))
                    .thenApply(o -> CommandResult.fromSuccess());
        }
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter responseText = Parameter
                .builder()
                .name(USER_PARMETER)
                .type(User.class)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(responseText);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ModMailSlashCommandNames.MODMAIL)
                .commandName(COMMAND_NAME)
                .build();

        return CommandConfiguration.builder()
                .name(COMMAND_NAME)
                .module(ModMailModuleDefinition.MODMAIL)
                .parameters(parameters)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
                .help(helpInfo)
                .supportsEmbedException(true)
                .templated(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModMailFeatureDefinition.MOD_MAIL;
    }

}
