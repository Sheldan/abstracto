package dev.sheldan.abstracto.experience.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureDefinition;
import dev.sheldan.abstracto.experience.config.ExperienceSlashCommandNames;
import dev.sheldan.abstracto.experience.model.template.LevelRole;
import dev.sheldan.abstracto.experience.model.template.LevelRolesModel;
import dev.sheldan.abstracto.experience.service.ExperienceRoleService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class LevelRoles extends AbstractConditionableCommand {

    private static final String LEVEL_ROLES_COMMAND = "levelRoles";
    private static final String LEVEL_ROLES_TEMPLATE_KEY = "levelRoles_response";

    @Autowired
    private ExperienceRoleService experienceRoleService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        MessageToSend messageToSend = getResponseModel(commandContext.getGuild().getIdLong());
        return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()))
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Long serverId = event.getGuild().getIdLong();
        MessageToSend messageToSend = getResponseModel(serverId);
        return interactionService.replyMessageToSend(messageToSend, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    private MessageToSend getResponseModel(Long serverId) {
        AServer server = serverManagementService.loadServer(serverId);
        List<LevelRole> levelRoles = experienceRoleService.loadLevelRoleConfigForServer(server);
        levelRoles = levelRoles.stream().sorted(Comparator.comparingInt(LevelRole::getLevel).reversed()).collect(Collectors.toList());
        LevelRolesModel model = LevelRolesModel
                .builder()
                .levelRoles(levelRoles)
                .build();
        return templateService.renderEmbedTemplate(LEVEL_ROLES_TEMPLATE_KEY, model, serverId);
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ExperienceSlashCommandNames.EXPERIENCE)
                .commandName(LEVEL_ROLES_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(LEVEL_ROLES_COMMAND)
                .module(ExperienceModuleDefinition.EXPERIENCE)
                .async(true)
                .slashCommandConfig(slashCommandConfig)
                .templated(true)
                .supportsEmbedException(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ExperienceFeatureDefinition.EXPERIENCE;
    }
}
