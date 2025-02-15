package dev.sheldan.abstracto.core.commands.config.profanity;

import dev.sheldan.abstracto.core.interaction.slash.CoreSlashCommandNames;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleDefinition;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.models.database.ProfanityGroup;
import dev.sheldan.abstracto.core.models.template.commands.ProfanityConfigModel;
import dev.sheldan.abstracto.core.service.management.ProfanityGroupManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class ShowProfanityConfig extends AbstractConditionableCommand {

    public static final String SHOW_PROFANITY_CONFIG_RESPONSE_TEMPLATE_KEY = "showProfanityConfig_response";
    public static final String SHOW_PROFANITY_CONFIG_COMMAND = "showProfanityConfig";
    @Autowired
    private ProfanityGroupManagementService profanityGroupManagementService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Long serverId = event.getGuild().getIdLong();
        MessageToSend message = getMessageToSend(serverId);
        return interactionService.replyMessageToSend(message, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    private MessageToSend getMessageToSend(Long serverId) {
        List<ProfanityGroup> groups = profanityGroupManagementService.getAllForServer(serverId);
        ProfanityConfigModel model = ProfanityConfigModel
                .builder()
                .profanityGroups(groups)
                .build();
        return templateService.renderEmbedTemplate(SHOW_PROFANITY_CONFIG_RESPONSE_TEMPLATE_KEY, model, serverId);
    }

    @Override
    public CommandConfiguration getConfiguration() {
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .defaultPrivilege(SlashCommandPrivilegeLevels.ADMIN)
                .rootCommandName(CoreSlashCommandNames.PROFANITY)
                .commandName(SHOW_PROFANITY_CONFIG_COMMAND)
                .build();


        return CommandConfiguration.builder()
                .name(SHOW_PROFANITY_CONFIG_COMMAND)
                .module(ConfigModuleDefinition.CONFIG)
                .templated(true)
                .async(true)
                .slashCommandConfig(slashCommandConfig)
                .supportsEmbedException(true)
                .help(helpInfo)
                .slashCommandOnly(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }
}
