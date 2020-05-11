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

@Component
public class Contact extends AbstractConditionableCommand {

    @Autowired
    private ModMailThreadService modMailThreadService;

    @Autowired
    private ModMailThreadManagementService modMailThreadManagementService;

    @Autowired
    private UserInServerManagementService userManagementService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        Member targetUser = (Member) commandContext.getParameters().getParameters().get(0);
        AUserInAServer user = userManagementService.loadUser(targetUser);
        ModMailThread existingThread = modMailThreadManagementService.getOpenModmailThreadForUser(user);
        if(existingThread != null) {
            ModMailThreadExistsModel model = (ModMailThreadExistsModel) ContextConverter.fromCommandContext(commandContext, ModMailThreadExistsModel.class);
            model.setExistingModMailThread(existingThread);
            MessageToSend messageToSend = templateService.renderEmbedTemplate("modmail_thread_already_exists", model);
            channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel());
        } else {
            FullUser fullUser = FullUser
                    .builder()
                    .aUserInAServer(user)
                    .member(targetUser)
                    .build();
            modMailThreadService.createModMailThreadForUser(fullUser, commandContext.getChannel(), false);
        }
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter responseText = Parameter.builder().name("user").type(Member.class).description("The user to contact").build();
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
