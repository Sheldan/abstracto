package dev.sheldan.abstracto.experience.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandAutoCompleteService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureDefinition;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureMode;
import dev.sheldan.abstracto.experience.config.ExperienceSlashCommandNames;
import dev.sheldan.abstracto.experience.exception.LevelActionNotFoundException;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import dev.sheldan.abstracto.experience.service.management.LevelActionManagementService;
import dev.sheldan.abstracto.experience.service.management.UserExperienceManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RemoveLevelAction extends AbstractConditionableCommand {

    private static final String COMMAND_NAME = "removeLevelAction";
    private static final String ACTION_PARAMETER_NAME = "action";
    private static final String LEVEL_PARAMETER_NAME = "level";
    private static final String MEMBER_PARAMETER_NAME = "member";

    private static final String RESPONSE_TEMPLATE = "removeLevelAction_response";

    @Autowired
    private LevelActionManagementService levelActionManagementService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private SlashCommandAutoCompleteService slashCommandAutoCompleteService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private UserExperienceManagementService userExperienceManagementService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String actionName = slashCommandParameterService.getCommandOption(ACTION_PARAMETER_NAME, event, String.class);
        AUserExperience userExperience = null;
        if(slashCommandParameterService.hasCommandOption(MEMBER_PARAMETER_NAME, event)) {
            Member member = slashCommandParameterService.getCommandOption(MEMBER_PARAMETER_NAME, event, Member.class);
            AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(member);
            userExperience = userExperienceManagementService.findUserInServer(aUserInAServer);
        }
        Integer level = slashCommandParameterService.getCommandOption(LEVEL_PARAMETER_NAME, event, Integer.class);
        AServer server = serverManagementService.loadServer(event.getGuild());
        log.info("Removing level action {} for level {} in server {}.", actionName, level, event.getGuild().getId());
        if(levelActionManagementService.getLevelAction(actionName, level, server, userExperience).isEmpty()) {
            throw new LevelActionNotFoundException();
        }
        levelActionManagementService.deleteLevelAction(level, server, actionName, userExperience);
        return interactionService.replyEmbed(RESPONSE_TEMPLATE, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public List<String> performAutoComplete(CommandAutoCompleteInteractionEvent event) {
        if(slashCommandAutoCompleteService.matchesParameter(event.getFocusedOption(), ACTION_PARAMETER_NAME)) {
            String input = event.getFocusedOption().getValue().toLowerCase();
            AServer server = serverManagementService.loadServer(event.getGuild());
            Set<String> availableLevelActions = levelActionManagementService.getLevelActionsOfServer(server)
                    .stream()
                    .map(levelAction -> levelAction.getAction().toLowerCase())
                    .collect(Collectors.toSet());
            if(!input.isEmpty()) {
                return availableLevelActions.stream().filter(s -> s.startsWith(input)).toList();
            } else {
                return new ArrayList<>(availableLevelActions);
            }
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter actionParameter = Parameter
                .builder()
                .name(ACTION_PARAMETER_NAME)
                .templated(true)
                .type(String.class)
                .supportsAutoComplete(true)
                .build();

        Parameter levelParameter = Parameter
                .builder()
                .name(LEVEL_PARAMETER_NAME)
                .templated(true)
                .type(Integer.class)
                .build();

        Parameter memberParameter = Parameter
                .builder()
                .name(MEMBER_PARAMETER_NAME)
                .templated(true)
                .optional(true)
                .type(Member.class)
                .build();

        List<Parameter> parameters = Arrays.asList(levelParameter, actionParameter, memberParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ExperienceSlashCommandNames.EXPERIENCE_CONFIG)
                .groupName("levelAction")
                .commandName("remove")
                .build();

        return CommandConfiguration.builder()
                .name(COMMAND_NAME)
                .module(ExperienceModuleDefinition.EXPERIENCE)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .supportsEmbedException(true)
                .slashCommandOnly(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ExperienceFeatureDefinition.EXPERIENCE;
    }

    @Override
    public List<FeatureMode> getFeatureModeLimitations() {
        return Arrays.asList(ExperienceFeatureMode.LEVEL_ACTION);
    }
}
