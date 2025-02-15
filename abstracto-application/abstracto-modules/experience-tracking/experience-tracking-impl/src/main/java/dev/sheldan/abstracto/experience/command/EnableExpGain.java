package dev.sheldan.abstracto.experience.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
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
 * Command to enable experience gain for a member
 */
@Component
public class EnableExpGain extends AbstractConditionableCommand {

    private static final String ENABLE_EXP_GAIN_COMMAND = "enableExpGain";
    private static final String ENABLE_EXP_GAIN_RESPONSE = "enableExpGain_response";
    private static final String MEMBER_PARAMETER = "member";

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
        Member member = (Member) commandContext.getParameters().getParameters().get(0);
        if(!member.getGuild().equals(commandContext.getGuild())) {
            throw new EntityGuildMismatchException();
        }
        AUserInAServer userInAServer = userInServerManagementService.loadOrCreateUser(member);
        aUserExperienceService.enableExperienceForUser(userInAServer);
        return CommandResult.fromSuccess();
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Member member = slashCommandParameterService.getCommandOption(MEMBER_PARAMETER, event, Member.class);
        if(!member.getGuild().equals(event.getGuild())) {
            throw new EntityGuildMismatchException();
        }
        AUserInAServer userInAServer = userInServerManagementService.loadOrCreateUser(member);
        aUserExperienceService.enableExperienceForUser(userInAServer);
        return interactionService.replyEmbed(ENABLE_EXP_GAIN_RESPONSE, event)
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
                .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
                .commandName(ENABLE_EXP_GAIN_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(ENABLE_EXP_GAIN_COMMAND)
                .slashCommandConfig(slashCommandConfig)
                .module(ExperienceModuleDefinition.EXPERIENCE)
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
