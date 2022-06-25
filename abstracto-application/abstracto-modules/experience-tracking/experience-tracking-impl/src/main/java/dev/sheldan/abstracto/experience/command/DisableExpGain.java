package dev.sheldan.abstracto.experience.command;

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
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureDefinition;
import dev.sheldan.abstracto.experience.config.ExperienceSlashCommandNames;
import dev.sheldan.abstracto.experience.service.AUserExperienceService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Command used to disable the experience gain for a specific member
 */
@Component
public class DisableExpGain extends AbstractConditionableCommand {

    private static final String MEMBER_PARAMETER = "member";
    private static final String DISABLE_EXP_GAIN_COMMAND = "disableExpGain";
    private static final String DISABLE_EXP_GAIN_RESPONSE = "disableExpGain_response";
    @Autowired
    private AUserExperienceService aUserExperienceService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        Member para = (Member) commandContext.getParameters().getParameters().get(0);
        if(!para.getGuild().equals(commandContext.getGuild())) {
            throw new EntityGuildMismatchException();
        }
        AUserInAServer userInAServer = userInServerManagementService.loadOrCreateUser(para);
        aUserExperienceService.disableExperienceForUser(userInAServer);
        return CommandResult.fromSuccess();
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Member para = slashCommandParameterService.getCommandOption(MEMBER_PARAMETER, event, Member.class);
        if(!para.getGuild().equals(event.getGuild())) {
            throw new EntityGuildMismatchException();
        }
        AUserInAServer userInAServer = userInServerManagementService.loadOrCreateUser(para);
        aUserExperienceService.disableExperienceForUser(userInAServer);
        return interactionService.replyEmbed(DISABLE_EXP_GAIN_RESPONSE, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter memberParameter = Parameter
                .builder()
                .name(MEMBER_PARAMETER)
                .templated(true)
                .type(Member.class)
                .build();
        List<Parameter> parameters = Arrays.asList(memberParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ExperienceSlashCommandNames.EXPERIENCE_CONFIG)
                .commandName(DISABLE_EXP_GAIN_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(DISABLE_EXP_GAIN_COMMAND)
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
    public FeatureDefinition getFeature() {
        return ExperienceFeatureDefinition.EXPERIENCE;
    }
}
