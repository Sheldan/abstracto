package dev.sheldan.abstracto.modmail.commands;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.modmail.config.ModMailFeatures;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import dev.sheldan.abstracto.modmail.models.template.ModMailThreadExistsModel;
import dev.sheldan.abstracto.modmail.service.ModMailThreadService;
import dev.sheldan.abstracto.modmail.service.management.ModMailThreadManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This command is used to create a thread with a member directly. If a thread already exists, this will post a link to
 * the {@link net.dv8tion.jda.api.entities.MessageChannel}
 */
@Component
@Slf4j
public class Contact extends AbstractConditionableCommand {

    @Autowired
    private ModMailThreadService modMailThreadService;

    @Autowired
    private ModMailThreadManagementService modMailThreadManagementService;

    @Autowired
    private UserInServerManagementService userManagementService;

    @Autowired
    private ChannelService channelService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        Member targetUser = (Member) commandContext.getParameters().getParameters().get(0);
        AUserInAServer user = userManagementService.loadUser(targetUser);
        // if this AUserInAServer already has an open thread, we should instead post a message
        // containing a link to the channel, instead of opening a new one
        if(modMailThreadManagementService.hasOpenModMailThreadForUser(user)) {
            log.info("Modmail thread for user {} in server {} already exists. Notifying user {}.", commandContext.getAuthor().getId(), commandContext.getGuild().getId(), user.getUserReference().getId());
            ModMailThreadExistsModel model = (ModMailThreadExistsModel) ContextConverter.fromCommandContext(commandContext, ModMailThreadExistsModel.class);
            ModMailThread existingThread = modMailThreadManagementService.getOpenModMailThreadForUser(user);
            model.setExistingModMailThread(existingThread);
            List<CompletableFuture<Message>> futures = channelService.sendEmbedTemplateInChannel("modmail_thread_already_exists", model, commandContext.getChannel());
            return FutureUtils.toSingleFutureGeneric(futures).thenApply(aVoid -> CommandResult.fromIgnored());
        } else {
            return modMailThreadService.createModMailThreadForUser(targetUser, null, commandContext.getChannel(), false, commandContext.getUndoActions())
                    .thenApply(aVoid -> CommandResult.fromSuccess());
        }
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter responseText = Parameter.builder().name("user").type(Member.class).templated(true).build();
        List<Parameter> parameters = Arrays.asList(responseText);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("contact")
                .module(ModMailModuleInterface.MODMAIL)
                .parameters(parameters)
                .async(true)
                .help(helpInfo)
                .supportsEmbedException(true)
                .templated(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return ModMailFeatures.MOD_MAIL;
    }

}
