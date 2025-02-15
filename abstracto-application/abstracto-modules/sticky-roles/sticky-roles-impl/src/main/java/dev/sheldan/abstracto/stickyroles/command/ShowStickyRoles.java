package dev.sheldan.abstracto.stickyroles.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.models.template.display.RoleDisplay;
import dev.sheldan.abstracto.stickyroles.config.StickyRolesFeatureDefinition;
import dev.sheldan.abstracto.stickyroles.config.StickyRolesSlashCommandNames;
import dev.sheldan.abstracto.stickyroles.model.database.StickyRole;
import dev.sheldan.abstracto.stickyroles.model.template.StickyRoleDisplayModel;
import dev.sheldan.abstracto.stickyroles.model.template.StickyRolesDisplayModel;
import dev.sheldan.abstracto.stickyroles.service.StickyRoleService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class ShowStickyRoles extends AbstractConditionableCommand {

    private static final String COMMAND_NAME = "showStickyRoles";
    private static final String RESPONSE_TEMPLATE = "showStickyRoles_response";

    @Autowired
    private StickyRoleService stickyRoleService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        List<StickyRole> stickyRoles = stickyRoleService.getStickyRolesForServer(event.getGuild());
        log.info("Showing sticky role config for {} roles in server {}.", stickyRoles.size(), event.getGuild().getIdLong());
        StickyRolesDisplayModel model = getModel(stickyRoles);
        return interactionService.replyEmbed(RESPONSE_TEMPLATE, model, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    private StickyRolesDisplayModel getModel(List<StickyRole> stickyRoles) {
        List<StickyRoleDisplayModel> displayRoles = stickyRoles
                .stream()
                .map(stickyRole -> StickyRoleDisplayModel
                        .builder()
                        .roleDisplay(RoleDisplay.fromARole(stickyRole.getRole()))
                        .sticky(stickyRole.getSticky())
                        .build())
                .toList();
        return StickyRolesDisplayModel
                .builder()
                .roles(displayRoles)
                .build();
    }

    @Override
    public CommandConfiguration getConfiguration() {

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(StickyRolesSlashCommandNames.STICKY_ROLES)
                .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
                .commandName("show")
                .build();

        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();
        return CommandConfiguration.builder()
                .name(COMMAND_NAME)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
                .slashCommandOnly(true)
                .supportsEmbedException(true)
                .help(helpInfo)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return StickyRolesFeatureDefinition.STICKY_ROLES;
    }
}
