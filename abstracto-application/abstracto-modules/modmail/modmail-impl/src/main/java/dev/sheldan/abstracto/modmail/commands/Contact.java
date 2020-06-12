package dev.sheldan.abstracto.modmail.commands;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.models.FullUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.modmail.config.ModMailFeatures;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import dev.sheldan.abstracto.modmail.models.template.ModMailThreadExistsModel;
import dev.sheldan.abstracto.modmail.service.ModMailThreadService;
import dev.sheldan.abstracto.modmail.service.management.ModMailThreadManagementService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * This command is used to create a thread with a member directly. If a thread already exists, this will post a link to
 * the {@link net.dv8tion.jda.api.entities.MessageChannel}
 */
@Component
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
    public CommandResult execute(CommandContext commandContext) {
        Member targetUser = (Member) commandContext.getParameters().getParameters().get(0);
        AUserInAServer user = userManagementService.loadUser(targetUser);
        // if this AUserInAServer already has an open thread, we should instead post a message
        // containing a link to the channel, instead of opening a new one
        if(modMailThreadManagementService.hasOpenModMailThreadForUser(user)) {
            ModMailThreadExistsModel model = (ModMailThreadExistsModel) ContextConverter.fromCommandContext(commandContext, ModMailThreadExistsModel.class);
            ModMailThread existingThread = modMailThreadManagementService.getOpenModMailThreadForUser(user);
            model.setExistingModMailThread(existingThread);
            channelService.sendEmbedTemplateInChannel("modmail_thread_already_exists", model, commandContext.getChannel());
        } else {
            FullUser fullUser = FullUser
                    .builder()
                    .aUserInAServer(user)
                    .member(targetUser)
                    .build();
            modMailThreadService.createModMailThreadForUser(fullUser, null, commandContext.getChannel(), false);
        }
        return CommandResult.fromSuccess();
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
                .help(helpInfo)
                .templated(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return ModMailFeatures.MOD_MAIL;
    }

}
