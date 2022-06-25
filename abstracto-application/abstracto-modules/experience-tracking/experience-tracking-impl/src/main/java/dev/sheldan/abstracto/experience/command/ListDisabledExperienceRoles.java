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
import dev.sheldan.abstracto.core.models.FullRole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureDefinition;
import dev.sheldan.abstracto.experience.config.ExperienceSlashCommandNames;
import dev.sheldan.abstracto.experience.model.database.ADisabledExpRole;
import dev.sheldan.abstracto.experience.model.template.DisabledExperienceRolesModel;
import dev.sheldan.abstracto.experience.service.management.DisabledExpRoleManagementService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Creates an embed containing the roles for which the experience gain has been disabled.
 */
@Component
public class ListDisabledExperienceRoles extends AbstractConditionableCommand {

    private static final String LIST_DISABLED_EXPERIENCE_ROLES_RESPONSE = "list_disabled_experience_roles";
    private static final String LIST_DISABLED_EXPERIENCE_ROLES_COMMAND = "listDisabledExperienceRoles";

    @Autowired
    private DisabledExpRoleManagementService disabledExpRoleManagementService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        Long serverId = commandContext.getGuild().getIdLong();
        MessageToSend messageToSend = getResponseModel(serverId, commandContext.getAuthor());
        List<CompletableFuture<Message>> futures = channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel());
        return FutureUtils.toSingleFutureGeneric(futures).thenApply(aVoid -> CommandResult.fromIgnored());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Long serverId = event.getGuild().getIdLong();
        MessageToSend messageToSend = getResponseModel(serverId, event.getMember());
        return interactionService.replyMessageToSend(messageToSend, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    private MessageToSend getResponseModel(Long serverId, Member member) {
        AServer server =  serverManagementService.loadServer(serverId);
        List<ADisabledExpRole> disabledRolesForServer = disabledExpRoleManagementService.getDisabledRolesForServer(server);
        DisabledExperienceRolesModel disabledExperienceRolesModel = DisabledExperienceRolesModel
                .builder()
                .member(member)
                .build();
        disabledRolesForServer.forEach(aDisabledExpRole -> {
            Role jdaRole = null;
            if(!aDisabledExpRole.getRole().getDeleted()) {
                jdaRole = roleService.getRoleFromGuild(aDisabledExpRole.getRole());
            }
            FullRole role = FullRole
                    .builder()
                    .role(aDisabledExpRole.getRole())
                    .serverRole(jdaRole)
                    .build();
            disabledExperienceRolesModel.getRoles().add(role);
        });
        return templateService.renderEmbedTemplate(LIST_DISABLED_EXPERIENCE_ROLES_RESPONSE, disabledExperienceRolesModel, serverId);
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();
        List<String> aliases = Arrays.asList("lsDisEpRoles");

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ExperienceSlashCommandNames.EXPERIENCE_CONFIG)
                .commandName(LIST_DISABLED_EXPERIENCE_ROLES_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(LIST_DISABLED_EXPERIENCE_ROLES_COMMAND)
                .module(ExperienceModuleDefinition.EXPERIENCE)
                .slashCommandConfig(slashCommandConfig)
                .templated(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .async(true)
                .aliases(aliases)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ExperienceFeatureDefinition.EXPERIENCE;
    }
}
