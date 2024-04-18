package dev.sheldan.abstracto.experience.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureDefinition;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureMode;
import dev.sheldan.abstracto.experience.config.ExperienceSlashCommandNames;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class ExpLevelUpNotification extends AbstractConditionableCommand {

    private static final String FLAG_PARAMETER = "newValue";
    private static final String EXP_LEVEL_UP_NOTIFICATION_COMMAND = "expLevelUpNotification";
    private static final String EXP_LEVEL_UP_NOTIFICATION_RESPONSE = "expLevelUpNotification_response";

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private AUserExperienceService aUserExperienceService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        Boolean newValue = (Boolean) commandContext.getParameters().getParameters().get(0);
        updateExpLevelNotification(commandContext.getAuthor(), newValue);
        return CommandResult.fromSuccess();
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Boolean newValue = slashCommandParameterService.getCommandOption(FLAG_PARAMETER, event, Boolean.class);
        updateExpLevelNotification(event.getMember(), newValue);
        return interactionService.replyEmbed(EXP_LEVEL_UP_NOTIFICATION_RESPONSE, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    private void updateExpLevelNotification(Member member, Boolean newValue) {
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(member);
        aUserExperienceService.setLevelUpNotification(aUserInAServer, newValue);
    }

    @Override
    public FeatureDefinition getFeature() {
        return ExperienceFeatureDefinition.EXPERIENCE;
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter memberParameter = Parameter
                .builder()
                .name(FLAG_PARAMETER)
                .templated(true)
                .type(Boolean.class)
                .build();
        List<Parameter> parameters = Arrays.asList(memberParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ExperienceSlashCommandNames.EXPERIENCE)
                .commandName(EXP_LEVEL_UP_NOTIFICATION_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(EXP_LEVEL_UP_NOTIFICATION_COMMAND)
                .module(ExperienceModuleDefinition.EXPERIENCE)
                .slashCommandConfig(slashCommandConfig)
                .causesReaction(true)
                .supportsEmbedException(true)
                .templated(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }


    @Override
    public List<FeatureMode> getFeatureModeLimitations() {
        return Arrays.asList(ExperienceFeatureMode.LEVEL_UP_NOTIFICATION);
    }
}
